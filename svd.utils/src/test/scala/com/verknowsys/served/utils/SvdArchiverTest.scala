/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.utils


import java.io.File
import java.lang.{System => JSystem}
import de.schlichtherle.io.{File => TFile}

import com.verknowsys.served._
import com.verknowsys.served.api._
import com.verknowsys.served.testing._


/**
 *  @author dmilith
 */
class SvdArchiverTest extends DefaultTest with Logging with SvdUtils {
    val defaultTestUserID = SvdConfig.defaultUserUID
    val tempDir = JSystem.getProperty("user.dir") / "tmp"
    val defaultUser = Some(SvdAccount(uid = defaultTestUserID))


    it should "be able to archive and unarchive given directory from svd directories and react properly in case of exceptions" in {
        evaluating { // not exists
            SvdArchiver(tempDir / "nothing", userAccount = defaultUser)
        } should produce [SvdArchiveNonExistantException]

        {
            val f = tempDir / "123"
            checkOrCreateDir(f)
            chmod(f, 0)

            evaluating { // exists but cannot read
                SvdArchiver(f, userAccount = defaultUser)
            } should produce [SvdArchiveACLException]

            evaluating {
                SvdArchiver.compact(f, userAccount = defaultUser)
            } should produce [SvdArchiveACLException]

            chmod(f, 0x777)
            rm_r(f)
        }

        evaluating {
            touch(tempDir / "somefile")
            SvdArchiver(JSystem.getProperty("user.dir") / "somefile", userAccount = defaultUser)
        } should produce [SvdArchiveNonExistantException]

        evaluating {
            touch(tempDir / "somefile")
            SvdArchiver.compact(JSystem.getProperty("user.dir") / "somefile", userAccount = defaultUser)
        } should produce [SvdArchiveNonExistantException]

        evaluating {
            SvdArchiver(null, userAccount = defaultUser)
        } should produce [SvdArchiveUnsupportedActionException]

        implicit def convTFtoS(a: File) = a.toString
        val baseDir = new File(JSystem.getProperty("user.dir") / "svd.user")
        val destFile = new File(SvdConfig.userHomeDir / "%d".format(defaultTestUserID) / SvdConfig.defaultBackupDir / "svd.user." + SvdConfig.defaultBackupFileExtension)
        SvdArchiver(baseDir, userAccount = defaultUser) // ServeD source directory
        SvdArchiver(baseDir, userAccount = defaultUser)
        destFile.exists should be(true)
        destFile.isFile should be(true)
        destFile.canRead should be(true)
        (destFile.length > 1000) should be(true)

        // unpack:
        SvdArchiver(destFile, userAccount = defaultUser)
        val resultFile = new File(SvdConfig.userHomeDir / "%d".format(defaultTestUserID) / SvdConfig.defaultBackupDir / "svd.user")
        resultFile.isDirectory should be(true)
        (resultFile.list.length > 2) should be(true)

        SvdArchiver.compact(baseDir, userAccount = defaultUser)
        rm_r(destFile)
        rm_r(resultFile)
    }


    it should "be able to recursively get svd directories" in {
        val dirs = SvdArchiver.gatherAllDirsRecursively(List(JSystem.getProperty("user.dir"))).map{_.getName} // ServeD source directories
        List("svd.root", "svd.user", "svd.api", "svd.utils", "main", "test", "scala", "java", "lib", "target", "resources").foreach{ elem =>
                dirs should contain (elem)
        }

    }



}
