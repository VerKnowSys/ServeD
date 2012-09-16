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
class SvdErrorWhileCopyingFiles(message: String) extends Exception(message)


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
        Unmount ZIP VFS to synchronize IO (for bigger files)
    */
    def unmountVFS =
        try {
            TFile.umount // unmount archive vfs
        } catch {
            case oops: ArchiveWarningException =>
                log.warn("ArchiveWarningException!")
                // Only instances of the class ArchiveWarningException exist in
                // the sequential chain of exceptions. We decide to ignore this.
            case ouch: ArchiveException =>
                // At least one exception occured which is not just an
                // ArchiveWarningException. This is a severe situation which
                // needs to be handled.
                log.error(ouch.sortAppearance.getStackTrace.mkString("\n"))

        } finally {
            log.debug("VFS unmounted")
        }


    /**
        @author dmilith
        Very fast way of dealing with adding new or removing old files from existing archive.
    */
    def addOrRemoveInArchive(fileOrDirectoryPath: String, prefix: String = "svd-archive-") = {
        val trimmedFileName = prefix + fileOrDirectoryPath.split("/").last
        log.info("Already found archive with name: %s.".format(trimmedFileName))
        log.debug("Source directory path: %s".format(fileOrDirectoryPath))

        val sourceFilesCore = new TFile(fileOrDirectoryPath)
        val sourceFiles = sourceFilesCore.listFiles
        if (sourceFiles == null) {
            if (sourceFilesCore.exists) {
                val exception = new SvdFSACLSecurityException("ACL access failure to file: %s. Operation is aborted.".format(fileOrDirectoryPath))
                log.error("Error occured in %s. Exception: %s".format(this.getClass.getName, exception))
                throw exception
            } else {
                val exception = new SvdFSACLSecurityException("Given source directory doesn't exists: %s. Operation is aborted.".format(fileOrDirectoryPath))
                log.error("Error occured in %s. Exception: %s".format(this.getClass.getName, exception))
                throw exception
            }
        } else {
            val sourceDirs = sourceFiles.filter{_.isDirectory}
            val virtualRootDestination = "%s%s.%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension)
            if (!new TFile(virtualRootDestination).exists) {
                val exception = new SvdFSACLSecurityException("Destination archive not found: %s. Nothing to add nor remove. Operation is aborted.".format(fileOrDirectoryPath))
                log.error("Error occured in %s. Exception: %s".format(this.getClass.getName, exception))
                throw exception
            }
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
                    } // don't forget to take files from root directory!

                val allDst = allArchiveDirs.flatMap{
                    _.listFiles.toList.par.map{
                        _.getPath.replaceFirst("^.*?%s.%s".format(trimmedFileName, SvdConfig.defaultBackupFileExtension), "")
                    }
                } ++ rawArchiveList.filter{_.isFile}.map{
                        _.getPath.replaceFirst("^.*?%s.%s".format(trimmedFileName, SvdConfig.defaultBackupFileExtension), "")
                    } // don't forget to take files from root directory!


                // compute added files diff:
                val diffAdded = allSrc.par.filterNot{ a => allDst.contains(a) }
                val diffRemoved = allDst.par.filterNot{ a => allSrc.contains(a) }
                log.info(" + %d added".format(diffAdded.length))
                log.info(" - %d removed".format(diffRemoved.length))

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
                    diffAdded.foreach{ diffPath =>
                        val source = new TFile(fileOrDirectoryPath + diffPath)
                        val dest = new TFile("%s%s.%s%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension, diffPath))
                        val result = source.archiveCopyAllTo(dest)
                        log.debug("Added file: %s (%s)".format(diffPath, result))
                        log.trace("Added list: %s".format(diffAdded.mkString(", ")))
                    }
                    diffRemoved.map{ diffPath =>
                        val result = new TFile("%s%s.%s%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension, diffPath)).delete
                        log.debug("Removed file: %s (%s)".format(diffPath, result))
                        log.trace("Removed list: %s".format(diffRemoved.mkString(", ")))
                    }
                }
                log.debug("Adding/Deleting files from archive taken: %dms".format(diffTimeOfRun))

            } else {
                log.warn("This shouldn't happen. What's wrong with %s?".format(rawArchiveList))
            }
        }
    }


    /**
        @author dmilith
        Call it to compress or decompress AES ZIP archive.
    */
    def apply(fileOrDirectoryPath: String, prefix: String = "svd-archive-", unpackDir: String = SvdConfig.defaultBackupDir + "CURRENT", exclude: List[String] = List(".lock", ".sock")) { // TODO: implement exclude list

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
                val trimmedFileName = prefix + fileOrDirectoryPath.split("/").last
                val sourceFilesCore = new TFile(fileOrDirectoryPath)
                val virtualRootDestination = "%s%s.%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension)
                if (sourceFilesCore.isFile) {
                    // packaging a single file
                    log.error("File given as source it's currently unsupported")
                    // TODO: implement single file archiving
                    // val timeOfRun = SvdUtils.bench {
                    //     val destination = new TFile("%s%s.%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension))
                    //     log.debug("Creating %s of %s".format(destination, sourceFilesCore.getName))
                    //     if (sourceFilesCore.archiveCopyTo(destination))
                    //         log.trace("Successfully copied file: %s".format(sourceFilesCore))
                    //     else
                    //         log.trace("Failure while copying file: %s".format(sourceFilesCore))
                    // }
                    // log.trace("Archive creation took: %dms".format(timeOfRun))

                } else {
                    val wholeOperationTime = SvdUtils.bench {

                        // packaging a directory
                        log.trace("Directory given as source")
                        if (!sourceFilesCore.canRead) {
                            val exception = new SvdFSACLSecurityException("ACL access failure to file: %s. Operation is aborted.".format(fileOrDirectoryPath))
                            log.error("Error occured in %s.\nException: %s\n\n%s".format(this.getClass.getName, exception, exception.getStackTrace.mkString("\n")))
                            throw exception
                        }

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
                            log.debug("Archive already found. Recreating it from source.")
                            val timeOfRun = SvdUtils.bench {
                                val source = new TFile(fileOrDirectoryPath)
                                val destination = new TFile("%s%s.%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension))
                                log.debug("Removing old archive")
                                destination.delete
                                log.debug("Creating %s of %s".format(destination, source))
                                if (source.archiveCopyAllTo(destination))
                                    log.trace("Successfully copied files")
                                else {
                                    val exception = new SvdErrorWhileCopyingFiles("Some files couldn't be archived! No disk space?")
                                    log.error("Error occured in %s.\nException: %s\n\n%s".format(this.getClass.getName, exception, exception.getStackTrace.mkString("\n")))
                                    throw exception
                                }
                            }
                            log.trace("Compression and encryption of archive took: %dms. It's packed in: %s".format(timeOfRun, "%s%s.%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension)))
                        }
                    }
                    log.debug("Whole operation taken: %dms".format(wholeOperationTime))

                }

        }
        unmountVFS
    }


}