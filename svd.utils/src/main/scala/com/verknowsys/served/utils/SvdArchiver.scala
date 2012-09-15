// © Copyright 2009-2012 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.utils

import com.verknowsys.served._
import de.schlichtherle.io._
import de.schlichtherle.key._
import de.schlichtherle.crypto.io.raes._
import java.io.File
import de.schlichtherle.io.{File => TFile}


/**
    @author dmilith
    SvdArchive Exception class
*/
class SvdInvalidOrBrokenArchiveException(message: String) extends Exception(message)
class SvdNonExistantSourceDirectoryException(message: String) extends Exception(message)


/**
    @author dmilith
    AES Key provider
*/
class SvdAESKeyProvider extends AesKeyProvider {

    def getCreateKey = SvdConfig.backupKey.toCharArray

    def getOpenKey = SvdConfig.backupKey.toCharArray

    def invalidOpenKey = {
        // This method is called whenever a key for an existing protected resource is invalid.
        throw new SvdInvalidOrBrokenArchiveException("Security key is invalid or archive is broken!")
    }

    def getKeyStrength = AesKeyProvider.KEY_STRENGTH_128

    def setKeyStrength(keyStrength: Int) = AesKeyProvider.KEY_STRENGTH_128
}


/**
    @author dmilith
    AES Key manager
*/
class SvdArchiverKeyManager extends KeyManager {
    mapKeyProviderType(classOf[AesKeyProvider], classOf[SvdAESKeyProvider])
}


/**
    @author dmilith
    SvdArchiver object to compress and decompress AES encrypted ZIP files with ease.
*/
object SvdArchiver extends Logging {

    System.setProperty("de.schlichtherle.key.KeyManager", SvdConfig.defaultBackupKeyManager)
    TFile.setDefaultArchiveDetector(new DefaultArchiveDetector(SvdConfig.defaultBackupFileExtension))

    /**
        @author dmilith
        Recursive method of gathering all directories of path in both folders and archives.
        @return Returns list of gathered files in whole directory tree.
    */
    def gatherAllDirsRecursively(rootDir: List[File], gathered: List[File] = Nil): List[File] = {
        if (rootDir.isEmpty) // TODO: do pattern match instead of if
            gathered
        else {
            val dir = rootDir.head
            val dirList = dir.listFiles.par.filter{_.isDirectory}.toList
            if (dirList.isEmpty) // TODO :pattern match instead of if
                gatherAllDirsRecursively(rootDir.tail, gathered ++ List(dir))
            else
                gatherAllDirsRecursively(rootDir.tail ++ dirList, gathered ++ List(dir))
        }
    }


    /**
        @author dmilith
        Simple hook for compressing and decompressing AES ZIP archives.
    */
    def apply(fileOrDirectoryPath: String, unpackDir: String = SvdConfig.defaultBackupDir + "/CURRENT") {
        val trimmedFileName = fileOrDirectoryPath.split("/").last
        trimmedFileName.matches(SvdConfig.defaultBackupFileMatcher) match {
            case true =>
                // case 1: decompression cause matched extension found
                val timeOfRun = SvdUtils.bench {
                    new TFile(fileOrDirectoryPath).archiveCopyAllTo(new File("%s".format(unpackDir)), ArchiveDetector.NULL)
                }
                log.trace("Decompression of archive took: %dms. It's unpacked in: %s".format(timeOfRun, unpackDir))

            case false =>
                // case 2: compression of whatever given as path
                val sourceFiles = new TFile(fileOrDirectoryPath).listFiles
                if (sourceFiles == null) {
                    throw new SvdNonExistantSourceDirectoryException("Source directory does not exists: %s".format(fileOrDirectoryPath))
                }
                val sourceDirs = sourceFiles.filter{_.isDirectory}

                val virtualRootDestination = "%s%s.%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension)

                // check for archive existance:
                if (new TFile(virtualRootDestination).listFiles == null) {
                    // creating archive
                    log.info("No archive found. Creating new one")
                    val timeOfRun = SvdUtils.bench {
                        new TFile(fileOrDirectoryPath).archiveCopyAllTo(new TFile("%s%s.%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension)))
                    }
                    log.trace("Compression and encryption of archive took: %dms. It's packed in: %s".format(timeOfRun, "%s%s.%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension)))

                } else {
                    // updating archive
                    log.info("Already found archive with name: %s. Will try to perform content update".format(trimmedFileName))

                    val gatheredDirList = gatherAllDirsRecursively(sourceDirs.toList).par.map{_.asInstanceOf[TFile]}
                    val rawArchiveList = new TFile("%s%s.%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension)).listFiles
                    if (rawArchiveList != null) {
                        val rootArchiveDirs = rawArchiveList.par.toList.filter{_.isDirectory}
                        val allArchiveDirs = gatherAllDirsRecursively(rootArchiveDirs).map{_.asInstanceOf[TFile]}
                        val allSrc = gatheredDirList.toList.flatMap{
                            _.listFiles.toList.map{
                                _.getPath.split(trimmedFileName).tail.mkString("/")
                            }
                        }

                        val allDst = allArchiveDirs.flatMap{
                            _.listFiles.toList.map{
                                _.getPath.split(trimmedFileName + ".%s".format(SvdConfig.defaultBackupFileExtension)).tail.mkString("/")
                            }
                        }

                        // detect changed files and update them when necessary
                        // val proxy = allArchiveDirs.par.flatMap{_.listFiles.toList.par}
                        // val nameProxy = trimmedFileName + ".%s".format(SvdConfig.defaultBackupFileExtension)
                        // log.debug("Searching for changed files")
                        // val timeOfRun = SvdUtils.bench {
                        //     for {
                        //         src <- gatheredDirList.par.flatMap{_.listFiles.toList}.par.filter{_.isFile}
                        //         dst <- proxy.filter{_.getPath.split(nameProxy
                        //             ).tail.mkString("/") == (src.getPath.split(trimmedFileName).tail.mkString("/"))}
                        //     } yield
                        //         if ( (src.lastModified/10000 != dst.lastModified/10000)) { // (src.getName == dst.getName) &&
                        //             log.debug("Changed file: %s".format(src))
                        //             val copyTask = TFile.cp_p(src, dst)
                        //             log.trace("File updated in archive? %s".format(copyTask))
                        //         }
                        // }
                        // log.trace("Changed files check took: %dms".format(timeOfRun))


                        val diffTimeOfRun = SvdUtils.bench {
                            log.trace("Source files total: " + allSrc.length)
                            log.trace("Archived files total: " + allDst.length)

                            val diffAdded = allSrc.filterNot{ a => allDst.contains(a) }
                            val diffRemoved = allDst.filterNot{ a => allSrc.contains(a) }
                            log.info(" + %d added".format(diffAdded.length))
                            log.info(" - %d removed".format(diffRemoved.length))

                            diffAdded.foreach{ diffPath =>
                                log.debug("Adding file: %s".format(diffPath))
                                new TFile(fileOrDirectoryPath + diffPath).archiveCopyAllTo(new TFile("%s%s.%s%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension, diffPath)))
                            }
                            diffRemoved.foreach{ diffPath =>
                                log.debug("Removing file: %s".format(diffPath))
                                new TFile("%s%s.%s%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension, diffPath)).delete
                            }
                        }
                        log.trace("Diff for added/ removed files + file update in archive took: %dms".format(diffTimeOfRun))

                        // perform check on existing archive, and try to do update:
                        log.debug("Updating archive contents")
                        val timeOfRun = SvdUtils.bench {
                            val archive = new TFile("%s%s.%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension))
                            TFile.umount()
                            TFile.update()
                            new TFile(fileOrDirectoryPath).archiveCopyAllTo(archive)
                        }
                        log.trace("Archive update took: %dms".format(timeOfRun))

                    // } else {
                    //     val diffDir = gatheredDirList.par.flatMap{_.getPath.split(trimmedFileName).tail.mkString("/")}
                    //     log.debug("Found missing directory: %s. Will be created and filled if not empty.".format(diffDir))
                    //     new TFile(fileOrDirectoryPath + diffDir).archiveCopyAllTo(new TFile("%s%s.%s%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension, diffDir)))
                    }

                }

        }
    }


}