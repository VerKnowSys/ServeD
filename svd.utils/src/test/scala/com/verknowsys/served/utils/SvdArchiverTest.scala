package com.verknowsys.served.utils


import de.schlichtherle.io._
import de.schlichtherle.key._
import de.schlichtherle.crypto.io.raes._
import java.io.{File, FileNotFoundException, DataOutputStream}
import de.schlichtherle.io.{File => TFile, FileOutputStream}
import scala.io.Source

import com.verknowsys.served._
import com.verknowsys.served.api._
import com.verknowsys.served.testing._
import com.verknowsys.served.utils.SvdSystemManagerUtils._


class SvdArchiverTest extends DefaultTest with Logging {
    val defaultTestUserID = 501
    val tempDir = System.getProperty("user.dir") / "tmp"


    it should "be able to create archive from svd directories and respond properly on exceptions" in {
        evaluating { // not exists
            SvdArchiver(tempDir / "nothing", userAccount = Some(SvdAccount(uid = defaultTestUserID)))
        } should produce [SvdArchiveNonExistantException]

        {
            val f = tempDir / "123"
            SvdUtils.checkOrCreateDir(f)
            chmod(f, 0)

            evaluating { // exists but cannot read
                SvdArchiver(f, userAccount = Some(SvdAccount(uid = defaultTestUserID)))
            } should produce [SvdArchiveACLException]

            chmod(f, 0x777)
            SvdUtils.rmdir(f)
        }

        evaluating {
            touch(tempDir / "somefile")
            SvdArchiver(System.getProperty("user.dir") / "somefile", userAccount = Some(SvdAccount(uid = defaultTestUserID)))
        } should produce [SvdArchiveNonExistantException]

        evaluating {
            SvdArchiver(null, userAccount = Some(SvdAccount(uid = defaultTestUserID)))
        } should produce [SvdArchiveUnsupportedActionException]

        implicit def convTFtoS(a: File) = a.toString
        val baseDir = new File(System.getProperty("user.dir") / "svd.user")
        val destFile = new File(SvdConfig.userHomeDir / "%d".format(defaultTestUserID) / SvdConfig.defaultBackupDir / "svd.user." + SvdConfig.defaultBackupFileExtension)
        SvdArchiver(baseDir, userAccount = Some(SvdAccount(uid = defaultTestUserID))) // ServeD source directory
        SvdArchiver(baseDir, userAccount = Some(SvdAccount(uid = defaultTestUserID)))
        destFile.exists should be(true)
        destFile.isFile should be(true)
        destFile.canRead should be(true)
        (destFile.length > 1000) should be(true)
        SvdUtils.rmdir(destFile)
    }


    it should "be able to recursively get svd directories" in {
        val dirs = SvdArchiver.gatherAllDirsRecursively(List(System.getProperty("user.dir"))).map{_.getName} // ServeD source directories
        List("svd.root", "svd.user", "svd.common", "svd.api", "svd.utils", "main", "test", "scala", "java", "lib", "target", "resources").foreach{ elem =>
                dirs should contain (elem)
        }

    }



}
