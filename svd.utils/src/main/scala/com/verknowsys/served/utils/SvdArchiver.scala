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

    // TODO: add checking of non existant source directory

    /**
        @author dmilith
        Simple hook for compressing and decompressing AES ZIP archives.
    */
    def apply(fileOrDirectoryPath: String, unpackDir: String = SvdConfig.defaultBackupDir + "/CURRENT") {
        val trimmedFileName = fileOrDirectoryPath.split("/").last
        trimmedFileName.matches(SvdConfig.defaultBackupFileMatcher) match {
            case true =>
                // case 1: decompression cause matched extension found
                new TFile(fileOrDirectoryPath).archiveCopyAllTo(new File("%s".format(unpackDir)), ArchiveDetector.DEFAULT, ArchiveDetector.NULL)

            case false =>
                // case 2: compression of whatever given as path
                val sourceFiles = new TFile(fileOrDirectoryPath).listFiles
                val sourceDirs = sourceFiles.filter{_.isDirectory}

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

                // check for archive existance:
                val virtualRootDestination = "%s%s.%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension)

                if (new TFile(virtualRootDestination).listFiles == null) {
                    log.info("No archive found. Creating new one")
                    new TFile(fileOrDirectoryPath).archiveCopyAllTo(new TFile("%s%s.%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension)))

                } else {
                    log.info("Found archive with name: %s. Checking for changes".format(trimmedFileName))
                    // perform check on existing archive, and try to do update:
                    val gatheredDirList = gatherAllDirsRecursively(sourceDirs.toList).par.map{_.asInstanceOf[TFile]}


                    // for {
                    //     source <- gatheredDirList
                    // } yield {
                    //     val destinationFiles = "%s%s.%s%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension, source.getPath.split(trimmedFileName).tail.mkString("/"))


                    //     // detect changed files and update them when necessary
                    //     for {
                    //         src <- gatheredDirList.flatMap{_.listFiles.filter{_.isFile}}
                    //         dst <- new TFile(destinationFiles).listFiles.filter{_.getName.matches(src.getName)}
                    //     } yield {
                    //         if ((src.getName == dst.getName) && (src.lastModified/10000 != dst.lastModified/10000)) {
                    //             log.debug("Changed and will be updated: %s -> %s".format(src, dst))
                    //             TFile.cp_p(src, dst)
                    //         }
                    //     }
                    // }

                    // checking for new directory name:
                    val rawArchiveList = new TFile("%s%s.%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension)).listFiles
                    if (rawArchiveList != null) {
                        val rootArchiveDirs = rawArchiveList.par.toList.filter{_.isDirectory}
                        val allArchiveDirs = gatherAllDirsRecursively(rootArchiveDirs).map{_.asInstanceOf[TFile]}
                        val allSrc = gatheredDirList.toList.flatMap{
                            a =>
                                a.listFiles.toList.map{
                                    _.getPath.split(trimmedFileName).tail.mkString("/")
                                }
                        }

                        val allDst = allArchiveDirs.flatMap{
                            a =>
                                a.listFiles.toList.map{
                                    _.getPath.split(trimmedFileName + ".%s".format(SvdConfig.defaultBackupFileExtension)).tail.mkString("/")
                                }
                        }


                        // detect changed files and update them when necessary
                        log.debug("Looking for time stamp diffs..")
                        for {
                            src <- gatheredDirList.par.flatMap{_.listFiles.toList.par.filter{_.isFile}}
                            dst <- allArchiveDirs.par.flatMap{
                                _.listFiles.toList.par.filter{
                                    _.getPath.split(trimmedFileName + ".%s".format(SvdConfig.defaultBackupFileExtension)).tail.mkString("/").matches(src.getPath.split(trimmedFileName).tail.mkString("/"))

                                }}
                        } yield {
                            if ( (src.lastModified/10000 != dst.lastModified/10000)) { // (src.getName == dst.getName) &&
                                log.debug("Changed and will be updated: %s -> %s".format(src, dst))
                                TFile.cp_p(src, dst)
                            } else {
                                log.debug("No change to %s".format(src))
                            }
                        }

                        log.debug("SRC: " + allSrc.length)
                        log.debug("DST: " + allDst.length)
                        val diff = allSrc.filterNot{ a => allDst.contains(a)}
                        log.debug("DIFF %s".format(diff))

                    } else {
                        val diffDir = gatheredDirList.par.flatMap{_.getPath.split(trimmedFileName).tail.mkString("/")}
                        log.debug("Found missing directory: %s. Will be created and filled if not empty.".format(diffDir))
                        new TFile(fileOrDirectoryPath + diffDir).archiveCopyAllTo(new TFile("%s%s.%s%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension, diffDir)))
                    }

                }

        }
    }


}