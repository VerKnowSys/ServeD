// © Copyright 2009-2012 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.utils

import com.verknowsys.served._
import de.schlichtherle.io._;
import de.schlichtherle.key._
import de.schlichtherle.crypto.io.raes._;
import de.schlichtherle.io.File;


/**
    @author dmilith
    SvdArchiver object to compress and decompress AES encrypted ZIP files with ease.
*/
object SvdArchiver {


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


    System.setProperty("de.schlichtherle.key.KeyManager", SvdConfig.defaultBackupKeyManager)
    File.setDefaultArchiveDetector(new DefaultArchiveDetector(SvdConfig.defaultBackupFileExtension))


    /**
        @author dmilith
        Simple hook for compressing and decompressing AES ZIP archives.
    */
    def apply(fileOrDirectoryPath: String, unpackDir: String = SvdConfig.defaultBackupDir + "/CURRENT") = {
        val trimmedFileName = fileOrDirectoryPath.split("/").last
        trimmedFileName.matches(SvdConfig.defaultBackupFileMatcher) match {
            case true =>
                // case 1: decompression cause matched extension found
                new File(fileOrDirectoryPath).archiveCopyAllTo(new File("%s".format(unpackDir)), ArchiveDetector.DEFAULT, ArchiveDetector.NULL)

            case false =>
                // case 2: compression of whatever given as path
                new File(fileOrDirectoryPath).archiveCopyAllTo(new File("%s%s.%s".format(SvdConfig.defaultBackupDir, trimmedFileName, SvdConfig.defaultBackupFileExtension)))
        }
    }


}