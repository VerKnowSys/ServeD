package com.verknowsys.served.utils


import com.verknowsys.served.SvdSpecHelpers._

import scala.util._
import org.apache.commons.io.FileUtils
import org.specs._
import java.io.File


class SvdUtilsTest extends Specification with Logging {

    "file existance checker should work properly" should {
        "return true if file exists" in {
            mkdir("/tmp/served/utils_test/foo")
            SvdUtils.fileExists("/tmp/served/utils_test/foo") must beTrue
        }

        "return false if file does not exist" in {
            rmdir("/tmp/served/utils_test")
            SvdUtils.fileExists("/tmp/served/utils_test/foo") must beFalse
        }
        
    }
    
    "chown should change owner" in {
        FileUtils.touch("/tmp/dupa007")
        SvdUtils.chown("/tmp/dupa007", user = 666, group = 6666) must beTrue
        SvdUtils.chown("/tmp/dupa007", user = 678, group = 567, recursive = false) must beTrue
        // SvdUtils.chmod("/tmp/dupa007", 0777) // 2011-01-31 01:01:42 - dmilith -NOTE: it's in OCT. it's same as chmod 511
        
        try { 
          new File("/tmp/dupa0078").delete
        } catch {
          case e: Exception =>
            fail("Shouldn't throw exception on deleting non existing file.")
        }
        
        try { 
          SvdUtils.chown("/tmp/dupa0078", user = 666, group = 6666) must beTrue
          fail("Chown on non existing folder/ file should throw an exception!")
        } catch {
          case e: Exception =>
        }
        
        val f = new File("/tmp/dupa_32745923").mkdir
        val g = new File("/tmp/dupa_32745923/abc").mkdir
        val h = new File("/tmp/dupa_32745923/xyz").mkdir
        FileUtils.touch("/tmp/dupa_32745923/abc/dupa00")
        FileUtils.touch("/tmp/dupa_32745923/abc/dupa01")
        FileUtils.touch("/tmp/dupa_32745923/dupa011")
        FileUtils.touch("/tmp/dupa_32745923/dupa004")
        FileUtils.touch("/tmp/dupa_32745923/dupa003")
        log.warn("Dynamic uid and gid should be set for all those files")
        SvdUtils.chown("/tmp/dupa_32745923", user = (Random.nextInt * 1000), group = (Random.nextInt * 2340), recursive = true) must beTrue
        SvdUtils.chown("/tmp/dupa_32745923/", user = (Random.nextInt * 1000), group = (Random.nextInt * 2340), recursive = true) must beTrue
    }
    
    "chmod should change permissions and count files in given folder properly" in {
        try { 
          SvdUtils.chmod("/tmpfdsa/dupadsf_327/xfdsayz", 0777, true)
          fail("Chmod should fail on attempt to chmod nonexistant file")
        } catch {
          case e: Exception =>
        }
        val f = new File("/tmp/dupa_327").mkdir
        val g = new File("/tmp/dupa_327/abc").mkdir
        val h = new File("/tmp/dupa_327/xyz").mkdir
        FileUtils.touch("/tmp/dupa_327/dupa00")
        FileUtils.touch("/tmp/dupa_32745923/xyz/dupa00")
        FileUtils.touch("/tmp/dupa_32745923/abc/dupa01")
        FileUtils.touch("/tmp/dupa_32745923/dupa011")
        FileUtils.touch("/tmp/dupa_32745923/dupa003")
        SvdUtils.chmod("/tmp/dupa_327", 0777, true) must beTrue
        SvdUtils.chmod("/tmp/dupa_327", 0777, false) must beTrue
        SvdUtils.chmod("/tmp/dupa_327/abc", 0777, true) must beTrue
        SvdUtils.chmod("/tmp/dupa_327/abc", 0777, false) must beTrue
        SvdUtils.chmod("/tmp/dupa_327/xyz", 0777, true) must beTrue
        SvdUtils.chmod("/tmp/dupa_327/xyz", 0777, false) must beTrue
        FileUtils.touch("/tmp/dupa_32745923/dupa011")
        SvdUtils.chmod("/tmp/dupa_32745923/dupa011", 0111, false) must beTrue
        SvdUtils.recursiveListFilesFromPath("/tmp/dupa_327").size must beEqual(3)
    }
    
    "recursive file listings should work properly without regex" in {
        val g = SvdUtils.recursiveListFilesFromPath(new File("/etc"))
        g must notBe(null)
        g.size must beGreaterThan(15)
        log.info("LISTA -r (no Regex): " + g.mkString(", "))
    }
    
    "recursive file listings should work properly with regex" in {
        val g = SvdUtils.recursiveListFilesByRegex(new File("/etc"), """.*passwd.*""".r)
        g must notBe(null)
        g.size must beGreaterThan(0)
        log.info("LISTA +r (with Regex): " + g.mkString(", "))
    }
    
}
