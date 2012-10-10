package com.verknowsys.served.utils


import com.verknowsys.served.testing._
import com.verknowsys.served._

import scala.util._
import org.apache.commons.io.FileUtils
import java.io.File


class SvdUtilsTest extends DefaultTest with Logging {


    it should "return true if file exists" in {
        val path = randomPath
        mkdir(path / "served" / "utils_test/foo")
        fileExists(path / "served" / "utils_test" / "foo") must be(true)
    }


    it should "return false if file does not exist" in {
        val path = randomPath
        rmdir(path / "served" / "utils_test")
        fileExists(path / "served" / "utils_test" / "foo") must be(false)
    }


    it should "chown should change owner" in {
        val path = randomPath
        val account = currentAccount.copy(uid = getUserUid)

        FileUtils.touch(path / "dupa007")
        chown(path / "dupa007", user = account.uid, group = SvdConfig.defaultUserGroup) must be(true)
        chown(path / "dupa007", user = account.uid, group = SvdConfig.defaultUserGroup, recursive = true) must be(true)
        chown(path / "dupa007", user = account.uid, group = SvdConfig.defaultUserGroup, recursive = false) must be(true)

        try {
          new File(path / "dupa0078").delete
        } catch {
          case e: Exception =>
            fail("Shouldn't throw exception on deleting non existing file.")
        }

        try {
          chown(path / "dupa0078", user = account.uid, group = SvdConfig.defaultUserGroup) must be(true)
          fail("Chown on non existing folder/ file should throw an exception!")
        } catch {
          case e: Exception =>
        }

        val f = new File(path / "dupa_32745923").mkdirs
        val g = new File(path / "dupa_32745923/abc").mkdirs
        val h = new File(path / "dupa_32745923/xyz").mkdirs
        FileUtils.touch(path / "dupa_32745923/abc/dupa00")
        FileUtils.touch(path / "dupa_32745923/abc/dupa01")
        FileUtils.touch(path / "dupa_32745923/dupa011")
        FileUtils.touch(path / "dupa_32745923/dupa004")
        FileUtils.touch(path / "dupa_32745923/dupa003")
        new File(path / "dupa_32745923/dupa003").exists must be(true)
        new File(path / "dupa_32745923/xyz").exists must be(true)
        new File(path / "dupa_32745923/xyz").isDirectory must be(true)
    }


    it should "chmod should change permissions and count files in given folder properly" in {
        val path = randomPath
        try {
          chmod(path / "dupadsf_327" / "xfdsayz", 0777, true)
          fail("Chmod should fail on attempt to chmod nonexistant file")
        } catch {
          case e: Exception =>
        }
        val f = new File(path / "dupa_327").mkdirs
        val g = new File(path / "dupa_327/abc").mkdirs
        val h = new File(path / "dupa_327/xyz").mkdirs
        FileUtils.touch(path / "dupa_327/dupa00")
        FileUtils.touch(path / "dupa_32745923/xyz/dupa00")
        FileUtils.touch(path / "dupa_32745923/abc/dupa01")
        FileUtils.touch(path / "dupa_32745923/dupa011")
        FileUtils.touch(path / "dupa_32745923/dupa003")
        chmod(path / "dupa_327", 0777, true) must be(true)
        chmod(path / "dupa_327", 0777, false) must be(true)
        chmod(path / "dupa_327/abc", 0777, true) must be(true)
        chmod(path / "dupa_327/abc", 0777, false) must be(true)
        chmod(path / "dupa_327/xyz", 0777, true) must be(true)
        chmod(path / "dupa_327/xyz", 0777, false) must be(true)
        FileUtils.touch(path / "dupa_32745923/dupa011")
        chmod(path / "dupa_32745923/dupa011", 0111, false) must be(true)
        recursiveListFilesFromPath(path / "dupa_327").size must be(4) // also count parent dir
    }


    it should "recursive file listings should work properly without regex" in {
        val g = recursiveListFilesFromPath(new File("/var"))
        g must not be(null)
        g.size must be >(5)
    }


    // 2011-06-09 20:35:19 - dmilith - PENDING: fix searching with regexp
    // "recursive file listings should work properly with regex" in {
    //         val g = recursiveListFilesByRegex(new File(System.getProperty("user.home") / ".ivy2"), """.*passwd.*""".r)
    //         g must notBe(null)
    //         g.size must beGreaterThan(0)
    //         log.info("LISTA +r (with Regex): " + g.mkString(", "))
    //     }

}
