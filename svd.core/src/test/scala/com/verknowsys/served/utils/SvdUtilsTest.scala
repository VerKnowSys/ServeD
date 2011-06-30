package com.verknowsys.served.utils


import com.verknowsys.served.systemmanager.SvdSystemManagerUtils
import com.verknowsys.served.SvdSpecHelpers._
import com.verknowsys.served.utils._
import com.verknowsys.served._

import scala.util._
import org.apache.commons.io.FileUtils
import org.specs._
import java.io.File


class SvdUtilsTest extends Specification with Logging {

    "file existance checker should work properly" should {
        "return true if file exists" in {
            val path = randomPath
            mkdir(path / "served" / "utils_test/foo")
            SvdUtils.fileExists(path / "served" / "utils_test" / "foo") must beTrue
        }

        "return false if file does not exist" in {
            val path = randomPath
            rmdir(path / "served" / "utils_test")
            SvdUtils.fileExists(path / "served" / "utils_test" / "foo") must beFalse
        }
        
    }

    // XXX: PENDING: FIXME: fix uid issue from Java
    // "chown should change owner" in {
    //     val account = currentAccount // XXX: this generates random User UID, and it's not possible to chown to it without root
    //     val path = randomPath
    //     FileUtils.touch(path / "dupa007")
    //     SvdSystemManagerUtils.chown(path / "dupa007", user = account.uid, group = account.gid) must beTrue
    //     SvdSystemManagerUtils.chown(path / "dupa007", user = account.uid, group = account.gid, recursive = true) must beTrue
    //     SvdSystemManagerUtils.chown(path / "dupa007", user = account.uid, group = account.gid, recursive = false) must beTrue
    //     
    //     try { 
    //       new File(path / "dupa0078").delete
    //     } catch {
    //       case e: Exception =>
    //         fail("Shouldn't throw exception on deleting non existing file.")
    //     }
    //     
    //     try { 
    //       SvdSystemManagerUtils.chown(path / "dupa0078", user = account.uid, group = account.gid) must beTrue
    //       fail("Chown on non existing folder/ file should throw an exception!")
    //     } catch {
    //       case e: Exception =>
    //     }
    //     
    //     val f = new File(path / "dupa_32745923").mkdirs
    //     val g = new File(path / "dupa_32745923/abc").mkdirs
    //     val h = new File(path / "dupa_32745923/xyz").mkdirs
    //     FileUtils.touch(path / "dupa_32745923/abc/dupa00")
    //     FileUtils.touch(path / "dupa_32745923/abc/dupa01")
    //     FileUtils.touch(path / "dupa_32745923/dupa011")
    //     FileUtils.touch(path / "dupa_32745923/dupa004")
    //     FileUtils.touch(path / "dupa_32745923/dupa003")
    //     new File(path / "dupa_32745923/dupa003").exists must beTrue
    //     new File(path / "dupa_32745923/xyz").exists must beTrue
    //     new File(path / "dupa_32745923/xyz").isDirectory must beTrue
    // }
    
    "chmod should change permissions and count files in given folder properly" in {
        val path = randomPath
        try { 
          SvdSystemManagerUtils.chmod(path / "dupadsf_327" / "xfdsayz", 0777, true)
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
        SvdSystemManagerUtils.chmod(path / "dupa_327", 0777, true) must beTrue
        SvdSystemManagerUtils.chmod(path / "dupa_327", 0777, false) must beTrue
        SvdSystemManagerUtils.chmod(path / "dupa_327/abc", 0777, true) must beTrue
        SvdSystemManagerUtils.chmod(path / "dupa_327/abc", 0777, false) must beTrue
        SvdSystemManagerUtils.chmod(path / "dupa_327/xyz", 0777, true) must beTrue
        SvdSystemManagerUtils.chmod(path / "dupa_327/xyz", 0777, false) must beTrue
        FileUtils.touch(path / "dupa_32745923/dupa011")
        SvdSystemManagerUtils.chmod(path / "dupa_32745923/dupa011", 0111, false) must beTrue
        SvdUtils.recursiveListFilesFromPath(path / "dupa_327").size must beEqual(3)
    }
    
    "recursive file listings should work properly without regex" in {
        val g = SvdUtils.recursiveListFilesFromPath(new File("/var"))
        g must notBe(null)
        g.size must beGreaterThan(5)
    }
    
    
    // 2011-06-09 20:35:19 - dmilith - PENDING: fix searching with regexp
    // "recursive file listings should work properly with regex" in {
    //         val g = SvdUtils.recursiveListFilesByRegex(new File(System.getProperty("user.home") / ".ivy2"), """.*passwd.*""".r)
    //         g must notBe(null)
    //         g.size must beGreaterThan(0)
    //         log.info("LISTA +r (with Regex): " + g.mkString(", "))
    //     }
    
}
