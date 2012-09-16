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
class SvdFSACLSecurityException(message: String) extends Exception(message)


/**
    @author dmilith
    AES Key provider
*/
class SvdAESKeyProvider extends AesKeyProvider with Logging {

    def getCreateKey = SvdConfig.backupKey.toCharArray

    def getOpenKey = SvdConfig.backupKey.toCharArray

    def invalidOpenKey = {
        // This method is called whenever a key for an existing protected resource is invalid.
        val exception = new SvdInvalidOrBrokenArchiveException("Security key is invalid or archive is broken!")
        log.error("Error occured in %s.\nException: %s\n\n%s".format(this.getClass.getName, exception, exception.getStackTrace.mkString("\n")))
        throw exception
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
            val dirListCore = dir.listFiles
            if ((dirListCore == null) && dir.isDirectory) {
                val exception = new SvdFSACLSecurityException("Invalid ACL on dir: %s. User must be owner of all archived files.".format(dir))
                    log.error("Error occured in %s.\nException: %s\n\n%s".format(this.getClass.getName, exception, exception.getStackTrace.mkString("\n")))
                throw exception
            } else {
                val dirList = dirListCore.filter{_.isDirectory}.toList
                if (dirList.isEmpty) // TODO :pattern match instead of if
                    gatherAllDirsRecursively(rootDir.tail, gathered ++ List(dir))
                else
                    gatherAllDirsRecursively(rootDir.tail ++ dirList, gathered ++ List(dir))
            }

        }
    }


    /**
        @author dmilith
        Simple hook for compressing and decompressing AES ZIP archives.
    */
    def apply(fileOrDirectoryPath: String, prefix: String = "svd-archive-", unpackDir: String = SvdConfig.defaultBackupDir + "CURRENT") {
        val trimmedFileName = prefix + fileOrDirectoryPath.split("/").last
        trimmedFileName.matches(SvdConfig.defaultBackupFileMatcher) match {
            case true =>
                // case 1: decompression cause matched extension found
                val timeOfRun = SvdUtils.bench {
                    val from = new TFile(fileOrDirectoryPath)
                    val to = new TFile("%s".format(unpackDir))
                    from.archiveCopyAllTo(to, ArchiveDetector.NULL) // NOTE: don't unpack archives recursively
                }
                log.trace("Decompression of archive: %s took: %dms. It's unpacked in: %s".format(fileOrDirectoryPath, timeOfRun, unpackDir))

            case false =>
                // case 2: compression of whatever given as path
                val sourceFilesCore = new TFile(fileOrDirectoryPath)
                if (sourceFilesCore.isFile) {
                    // packaging a single file
                    log.trace("File given as source")
                    log.error("[Not Yet Implemented]")
                    return
                } else {
                    // packaging a directory
                    log.trace("Directory given as source")
                    if (!sourceFilesCore.canRead) {
                        val exception = new SvdFSACLSecurityException("ACL access failure to file: %s. Operation is aborted.".format(fileOrDirectoryPath))
                        log.error("Error occured in %s.\nException: %s\n\n%s".format(this.getClass.getName, exception, exception.getStackTrace.mkString("\n")))
                        throw exception
                    }

                    val sourceFiles = sourceFilesCore.listFiles
                    // if (sourceFiles == null) {
                    //     val exception = new SvdFSACLSecurityException("ACL access failure to file: %s. Operation is aborted.".format(fileOrDirectoryPath))
                    //     log.error("Error occured in %s. Exception: %s".format(this.getClass.getName, exception))
                    //     throw exception
                    // } else {
                        val sourceDirs = sourceFiles.filter{_.isDirectory}
                        val virtualRootDestination = "%s%s.%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension)

                        // check for archive existance:
                        if (new TFile(virtualRootDestination).listFiles == null) {
                            // creating archive
                            log.info("No archive found. Creating new one")
                            val timeOfRun = SvdUtils.bench {
                                val source = new TFile(fileOrDirectoryPath)
                                val destination = new TFile("%s%s.%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension))
                                log.debug("Creating %s of %s".format(destination, source))
                                if (source.archiveCopyAllTo(destination))
                                    log.trace("Successfully copied files")
                                else
                                    log.trace("Failure while copying files")
                            }
                            log.trace("Compression and encryption of archive took: %dms. It's packed in: %s".format(timeOfRun, "%s%s.%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension)))

                        } else {
                            // updating archive
                            log.info("Already found archive with name: %s. Will try to perform content update".format(trimmedFileName))
                            log.debug("Source directory path: %s".format(fileOrDirectoryPath))

                            // perform check on existing archive, and try to do update:
                            log.debug("Updating archive contents")
                            val timeOfRun = SvdUtils.bench {
                                val archive = new TFile("%s%s.%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension))
                                // TFile.update()
                                val sourcePath = new TFile(fileOrDirectoryPath)
                                sourcePath.archiveCopyAllTo(archive)
                            }
                            log.trace("Archive update took: %dms".format(timeOfRun))

                            val gatheredDirList = gatherAllDirsRecursively(sourceDirs.toList).par //.map{_.asInstanceOf[TFile]}
                            val rawArchiveList = new TFile("%s%s.%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension)).listFiles
                            if (rawArchiveList != null) {
                                val rootArchiveDirs = rawArchiveList.par.toList.filter{_.isDirectory}
                                val allArchiveDirs = gatherAllDirsRecursively(rootArchiveDirs).map{_.asInstanceOf[TFile]}
                                val allSrc = (gatheredDirList).par.toList.flatMap{
                                    _.listFiles.toList.par.map{
                                        _.getPath.replaceFirst("^.*?%s".format(fileOrDirectoryPath), "")
                                    }
                                } ++ sourceFiles.filter{_.isFile}.map{
                                        _.getPath.replaceFirst("^.*?%s".format(fileOrDirectoryPath), "")
                                    }
                                    // don't forget to take files from root directory!

                                val allDst = allArchiveDirs.flatMap{
                                    _.listFiles.toList.par.map{
                                        _.getPath.replaceFirst("^.*?%s.%s".format(trimmedFileName, SvdConfig.defaultBackupFileExtension), "")
                                    }
                                } ++ rawArchiveList.filter{_.isFile}.map{
                                        _.getPath.replaceFirst("^.*?%s.%s".format(trimmedFileName, SvdConfig.defaultBackupFileExtension), "")
                                    } // don't forget to take files from root directory!


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

                                    val diffAdded = allSrc.par.filterNot{ a => allDst.contains(a) }
                                    val diffRemoved = allDst.par.filterNot{ a => allSrc.contains(a) }
                                    log.info(" + %d added".format(diffAdded.length))
                                    log.info(" - %d removed".format(diffRemoved.length))

                                    diffAdded.foreach{ diffPath =>
                                        val source = new TFile(fileOrDirectoryPath + diffPath)
                                        val dest = new TFile("%s%s.%s%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension, diffPath))
                                        val result = source.archiveCopyAllTo(dest)
                                        log.debug("Added file: %s (%s)".format(diffPath, result))
                                        log.trace("Added list: %s".format(diffAdded.mkString(", ")))
                                    }
                                    diffRemoved.foreach{ diffPath =>
                                        val result = new TFile("%s%s.%s%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension, diffPath)).delete
                                        log.debug("Removed file: %s (%s)".format(diffPath, result))
                                        log.trace("Removed list: %s".format(diffRemoved.mkString(", ")))
                                    }
                                }
                                log.trace("Diff for added/ removed files + file update in archive took: %dms".format(diffTimeOfRun))


                            // } else {
                            //     val diffDir = gatheredDirList.par.flatMap{_.getPath.split(trimmedFileName).tail.mkString("/")}
                            //     log.debug("Found missing directory: %s. Will be created and filled if not empty.".format(diffDir))
                            //     new TFile(fileOrDirectoryPath + diffDir).archiveCopyAllTo(new TFile("%s%s.%s%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension, diffDir)))
                            }

                        }

                }

        }
    }


}