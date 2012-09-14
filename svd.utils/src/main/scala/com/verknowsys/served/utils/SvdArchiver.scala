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
    AES Key provider
*/
class SvdAESKeyProvider extends AesKeyProvider {

    def getCreateKey = SvdConfig.backupKey.toCharArray

    def getOpenKey = SvdConfig.backupKey.toCharArray

    def invalidOpenKey = {
        // This method is called whenever a key for an existing protected resource is invalid.
        throw new UnsupportedOperationException("Cannot handle invalid keys!")
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
object SvdArchiver {

    System.setProperty("de.schlichtherle.key.KeyManager", SvdConfig.defaultBackupKeyManager)
    TFile.setDefaultArchiveDetector(new DefaultArchiveDetector(SvdConfig.defaultBackupFileExtension))


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
                        val dirList = dir.listFiles.filter{_.isDirectory}.toList
                        if (dirList.isEmpty) // TODO :pattern match instead of if
                            gatherAllDirsRecursively(rootDir.tail, gathered ++ List(dir))
                        else
                            gatherAllDirsRecursively(rootDir.tail ++ dirList, gathered ++ List(dir))
                    }
                }

                // check for archive existance:
                val virtualRootDestination = "%s%s.%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension)
                if (new TFile(virtualRootDestination).listFiles == null) {
                    println("No archive found. Creating new one")
                    new TFile(fileOrDirectoryPath).archiveCopyAllTo(new TFile("%s%s.%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension)))

                } else {
                    println("Found archive. Checking for changes")
                    // perform check on existing archive, and try to do update:
                    val gatheredDirList = gatherAllDirsRecursively(sourceDirs.toList).map{_.asInstanceOf[TFile]}

                    for {
                        source <- gatheredDirList
                    } yield {
                        val destinationFiles = "%s%s.%s%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension, source.getPath.split(trimmedFileName).tail.mkString("/"))


                        for {
                            src <- source.listFiles.filter{_.isFile}
                            dst <- new TFile(destinationFiles).listFiles.filter{_.getName.matches(src.getName)}
                        } yield {
                            if ((src.getName == dst.getName) && (src.lastModified/10000 != dst.lastModified/10000)) {
                                println("Changed and will be updated: %s -> %s".format(src, dst))
                                TFile.cp_p(src, dst)
                            }
                            // val unfiltered = new TFile(destinationFiles).listFiles.toList
                            // val srcFile = src.getPath.split(trimmedFileName).tail.mkString("/")
                            // val listOfNonExistantFiles = unfiltered.filterNot{_.getPath.contains(srcFile)}
                            // if (listOfNonExistantFiles.length == 0) {
                            //     println("New file/ No file: %s".format(srcFile))
                            //     // TFile.cp_p(src, dst)
                            // }
                        }
                    }
                }

        }
    }


}