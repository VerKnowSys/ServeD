// © Copyright 2009-2012 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.utils

import com.verknowsys.served._
import de.schlichtherle.io._
import de.schlichtherle.key._
import de.schlichtherle.crypto.io.raes._
import java.io.{File, FileNotFoundException, DataOutputStream}
import de.schlichtherle.io.{File => TFile, FileOutputStream}
import scala.io.Source

/**
    @author dmilith
    SvdArchive Exception classes
*/
class SvdArchiveInvalidRAESKeyException(message: String) extends Exception(message)
class SvdArchiveNonExistantException(message: String) extends Exception(message)
class SvdArchiveACLException(message: String) extends Exception(message)
class SvdArchiveUnsupportedActionException(message: String) extends Exception(message)


/**
    @author dmilith
    AES Key provider
*/
class SvdAESKeyProvider extends AesKeyProvider with Logging {

    def getCreateKey = SvdConfig.backupKey.toCharArray

    def getOpenKey = SvdConfig.backupKey.toCharArray

    def invalidOpenKey = {
        // This method is called whenever a key for an existing protected resource is invalid.
        SvdUtils.throwException[SvdArchiveInvalidRAESKeyException]("Security key is invalid or archive is broken!")
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
                SvdUtils.throwException[SvdArchiveACLException]("Invalid ACL on dir: %s. User must be owner of all archived files.".format(dir))
                Nil
            } else {
                val dirList = dirListCore.filter{_.isDirectory}
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
            log.trace("Unmounting VFS")
            val umountTime = SvdUtils.bench {
                TFile.umount // unmount archive vfs
            }
            log.debug("Unmounting done in %dms".format(umountTime))
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
        Perform update of changed/new files in archive.
        It updates only changed files.
        It will use disk IO _read_ only for reading archive file list and timestamp.
        This will use disk IO _write_ only if differences were found in existing files timestamps.
        WARNING: This function wont delete old files!
    */
    def updateByTimeStampDiff(fileOrDirectoryPath: String, prefix: String = "svd-archive-") = {
        val trimmedFileName = prefix + fileOrDirectoryPath.split("/").last
        // log.info("Found archive with name: %s.".format(trimmedFileName))
        // log.debug("Source directory path: %s".format(fileOrDirectoryPath))

        val sourceFilesCore = new TFile(fileOrDirectoryPath)
        val sourceFiles = sourceFilesCore.listFiles
        if (sourceFiles == null) {
            if (sourceFilesCore.exists)
                SvdUtils.throwException[SvdArchiveACLException]("ACL access failure to file: %s. Operation is aborted.".format(fileOrDirectoryPath))
            else
                SvdUtils.throwException[SvdArchiveNonExistantException]("Given source directory doesn't exists: %s. Operation is aborted.".format(fileOrDirectoryPath))

        } else {
            val sourceDirs = sourceFiles.filter{_.isDirectory}
            val virtualRootDestination = "%s%s.%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension)
            val virtualRootFile = new TFile(virtualRootDestination)
            if (!virtualRootFile.exists) {
                log.debug("Virtual archive root doesn't exists and will be created")
                virtualRootFile.mkdirs
            }
            // if (!new TFile(virtualRootDestination).exists)
            //     SvdUtils.throwException[SvdArchiveNonExistantException]("Destination archive not found: %s. Nothing to add nor remove. Operation is aborted.".format(fileOrDirectoryPath))

            val gatheredDirList = gatherAllDirsRecursively(sourceDirs.toList) //.map{_.asInstanceOf[TFile]}
            val rawArchiveList = new TFile("%s%s.%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension)).listFiles
            if (rawArchiveList != null) {
                val rootArchiveDirs = rawArchiveList.filter{_.isDirectory} //.toList
                val allArchiveDirs = gatherAllDirsRecursively(rootArchiveDirs.toList) //.map{_.asInstanceOf[TFile]}

                log.trace("Loading source files")
                val prebuiltSrcPrefix = "^.*?" + fileOrDirectoryPath
                val allSrc = (gatheredDirList).flatMap {
                    _.listFiles.map{
                        _.getPath.replaceFirst(prebuiltSrcPrefix, "")
                    }} ++ sourceFiles.filter{_.isFile}.map { // append archive root directory files
                        _.getPath.replaceFirst(prebuiltSrcPrefix, "")
                    }.par

                // detect changed files and update them when necessary
                val proxy = allArchiveDirs.flatMap{_.listFiles}
                val amountOfFiles = proxy.length
                val nameProxy = trimmedFileName + ".%s".format(SvdConfig.defaultBackupFileExtension)
                log.info("Total files in archive: %d".format(amountOfFiles))
                log.debug("Searching for changed files.")
                val timeOfRun = SvdUtils.bench {
                val srcBase = fileOrDirectoryPath
                val archBase = SvdConfig.defaultBackupDir + nameProxy

                val symlinkMetadataFile = "/." + trimmedFileName + "-archive-metadata.symlinks"
                val symlinkArchiveFileName = SvdConfig.temporaryDir + symlinkMetadataFile
                val symlinkArchFile = new TFile(symlinkArchiveFileName)

                val fos = new FileOutputStream(symlinkArchFile, true) // append mode true
                val dos = new DataOutputStream(fos)

                allSrc.map {
                    src =>
                        val srcFile = new TFile(srcBase + src)
                        val srcFileTrimmed = (srcBase + src).replaceFirst("^.*?" + fileOrDirectoryPath, "")
                        val archFile = new TFile(archBase + src)
                        val difference = srcFile.lastModified - archFile.lastModified // NOTE: .lastModified() on non existant file will return 0, hence we can use it to add new files really fast

                        if (SvdSymlink.isSymlink(srcFile)) {
                            log.trace("File " + srcFile + " is a symlink pointing to: " + SvdSymlink.getSymlinkDestination(srcFile))
                            try {
                                dos.writeBytes(srcFileTrimmed + "**" + SvdSymlink.getSymlinkDestination(srcFile) + "\n")
                            } catch {
                                case e: Exception =>
                                    log.error("Caught exception while writing to " + symlinkArchFile + ": " + e)
                            }

                        } else {
                            if (difference > 1000) { // XXX : hardcode?
                                try {
                                    TFile.cp_p(
                                        srcFile,
                                        archFile)
                                    log.trace("Added/Changed file: %s. Difference: %d".format(src, difference))
                                } catch {
                                    case e: FileNotFoundException =>
                                        if (srcFile.isDirectory) {
                                            archFile.mkdirs()
                                        }
                                }
                            } else {
                                log.trace("File unchanged: %s Difference: %d".format(src, difference))
                            }
                        }
                }
                fos.close
                dos.close

                log.debug("Copying symlink metadata: %s to archive".format(symlinkArchFile))
                TFile.cp_p(
                    symlinkArchFile,
                    new TFile(archBase + symlinkMetadataFile))

                log.debug("Deleting symlink metadata temporary file: %s".format(symlinkArchFile))
                symlinkArchiveFileName.delete

                }
                val perSecond = amountOfFiles * 1000.0 / timeOfRun
                val perMinute = amountOfFiles * 60000.0 / timeOfRun
                if (perSecond == perMinute)
                    log.debug("Changed files check took: %dms.".format(timeOfRun))
                else
                    log.debug("Changed files check took: %dms. So it's %2.2f per minute or %2.2f per second.".format(timeOfRun, perMinute, perSecond))
            }
        }
    }


    /**
        @author dmilith
        This will use disk IO _read_, only for reading archive file list.
        This will use disk IO _write_, only for removing files from archive which were already deleted from file system.
    */
    def compact(fileOrDirectoryPath: String, prefix: String = "svd-archive-") = {
        val trimmedFileName = prefix + fileOrDirectoryPath.split("/").last
        log.debug("Source directory path: %s".format(fileOrDirectoryPath))
        log.info("Found archive with name: %s.".format(trimmedFileName))

        val sourceFilesCore = new TFile(fileOrDirectoryPath)
        val sourceFiles = sourceFilesCore.listFiles
        if (sourceFiles == null) {
            if (sourceFilesCore.exists)
                SvdUtils.throwException[SvdArchiveACLException]("ACL access failure to file: %s. Operation is aborted.".format(fileOrDirectoryPath))
            else
                SvdUtils.throwException[SvdArchiveNonExistantException]("Given source directory doesn't exists: %s. Operation is aborted.".format(fileOrDirectoryPath))

        } else {
            val sourceDirs = sourceFiles.filter{_.isDirectory}
            val virtualRootDestination = "%s%s.%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension)
            val virtualRootFile = new TFile(virtualRootDestination)
            if (!virtualRootFile.exists) {
                SvdUtils.throwException[SvdArchiveNonExistantException]("Destination archive not found: %s. Nothing to add nor remove. Operation is aborted.".format(fileOrDirectoryPath))
            }

            val gatheredDirList = gatherAllDirsRecursively(sourceDirs.toList) //.map{_.asInstanceOf[TFile]}
            val rawArchiveList = new TFile(SvdConfig.defaultBackupDir + trimmedFileName + "." + SvdConfig.defaultBackupFileExtension).listFiles
            val symlinkMetadataFile = "/." + trimmedFileName + "-archive-metadata.symlinks"
            if (rawArchiveList != null) {
                val diffTimeOfRun = SvdUtils.bench {
                    val rootArchiveDirs = rawArchiveList.filter{_.isDirectory}
                    val allArchiveDirs = gatherAllDirsRecursively(rootArchiveDirs.toList) //.map{_.asInstanceOf[TFile]}

                    log.trace("Loading source files")
                    val allSrc = (gatheredDirList.flatMap{
                        _.listFiles.par.map{
                            _.getPath.replaceFirst("^.*?" + fileOrDirectoryPath, "")
                        }} ++ sourceFiles.filter{_.isFile}.par.map{ // append files from archive root directory
                            _.getPath.replaceFirst("^.*?" + fileOrDirectoryPath, "")}).filterNot{_.getPath == symlinkMetadataFile}

                    log.trace("Loading archive files")
                    val allDst = (allArchiveDirs.flatMap{
                        _.listFiles.map{
                            _.getPath.replaceFirst("^.*?" + trimmedFileName + "." + SvdConfig.defaultBackupFileExtension, "")
                        }} ++ rawArchiveList.filter{_.isFile}.map{ // append archive root directory files
                            _.getPath.replaceFirst("^.*?" + trimmedFileName + "." + SvdConfig.defaultBackupFileExtension, "")
                        }).filterNot{_.getPath == symlinkMetadataFile}

                    val diffRemoved = allDst.filterNot{ a => allSrc.contains(a) }.par
                    log.info(" - %d removed".format(diffRemoved.length))
                    log.trace("Source files total: " + allSrc.length)
                    log.trace("Archived files total: " + allDst.length)
                    val baseName = SvdConfig.defaultBackupDir + trimmedFileName + "." + SvdConfig.defaultBackupFileExtension
                    diffRemoved.foreach{ diffPath =>
                        val result = new TFile(baseName + diffPath).delete
                        log.debug("Removed file: " + diffPath + " (" + result + ")")
                        log.trace("Removed list: %s".format(diffRemoved.mkString(", ")))
                    }
                }
                log.debug("Compacting archive took: %dms".format(diffTimeOfRun))

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
                if (!new TFile(fileOrDirectoryPath).exists)
                    SvdUtils.throwException[SvdArchiveACLException]("Given source archive doesn't exists: %s. Operation is aborted.".format(fileOrDirectoryPath))

                val timeOfRun = SvdUtils.bench {
                    val from = new TFile(fileOrDirectoryPath)
                    val to = new TFile("%s".format(unpackDir))
                    from.archiveCopyAllTo(to, ArchiveDetector.NULL) // NOTE: don't unpack archives recursively

                    log.debug("Files unpacked. Now reading symlinks metadata from archive")
                    val symlinkMetadataFile = "/." + fileOrDirectoryPath.split("/").last.replaceFirst(".%s".format(SvdConfig.defaultBackupFileExtension), "") + "-archive-metadata.symlinks"
                    val symlinksMetadata = Source.fromFile(unpackDir + symlinkMetadataFile).getLines.toList
                    log.trace("Symlink metadata: %s".format(symlinksMetadata.mkString(", ")))
                    symlinksMetadata.map{ _.split("\\*\\*") }.foreach {
                        entry =>
                            val result = SvdSymlink.makeSymlink(unpackDir + entry.head, entry.tail.head)
                            log.trace("Creating %s with destination %s. Result: %s".format(unpackDir + entry.head, entry.tail.head, result))
                    }

                }
                log.debug("Decompression of archive: %s took: %dms. It's unpacked in: %s".format(fileOrDirectoryPath, timeOfRun, unpackDir))

            case false =>
                // case 2: compression of whatever given as path
                val trimmedFileName = prefix + fileOrDirectoryPath.split("/").last
                val sourceFilesCore = new TFile(fileOrDirectoryPath)
                val virtualRootDestination = "%s%s.%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension)
                if (sourceFilesCore.isFile)
                    // packaging a single file
                    SvdUtils.throwException[SvdArchiveUnsupportedActionException]("File given as source is currently unsupported".format(fileOrDirectoryPath))
                else {
                    val wholeOperationTime = SvdUtils.bench {
                        // packaging a directory
                        log.trace("Directory given as source")
                        if (!sourceFilesCore.canRead)
                            SvdUtils.throwException[SvdArchiveACLException]("ACL access failure to file: %s. Operation is aborted.".format(fileOrDirectoryPath))

                        log.debug("Composing archive from source files")
                        updateByTimeStampDiff(fileOrDirectoryPath, prefix)

                    }
                    log.debug("Whole operation took: %dms".format(wholeOperationTime))
                }
        }
        unmountVFS
    }


}