package com.verknowsys.served.utils


import com.verknowsys.served.SvdSpecHelpers._

import org.apache.commons.io.FileUtils
import org.specs._
import java.io.File


class SvdUtilsTest extends Specification {

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
        SvdUtils.chown("/tmp/dupa007", user = 666, group = 6666)
        SvdUtils.chown("/tmp/dupa007", user = 678, group = 567, recursive = false)
        // SvdUtils.chmod("/tmp/dupa007", 0777) // 2011-01-31 01:01:42 - dmilith -NOTE: it's in OCT. it's same as chmod 511
        
        try { 
          new File("/tmp/dupa0078").delete
        } catch {
          case e: Exception =>
            fail("Shouldn't throw exception on deleting non existing file.")
        }
        
        try { 
          SvdUtils.chown("/tmp/dupa0078", user = 666, group = 6666)
          fail("Chown on non existing folder/ file should throw an exception!")
        } catch {
          case e: Exception =>
        }
        
        val f = new File("/tmp/dupa_32745923").mkdir
        FileUtils.touch("/tmp/dupa_32745923/dupa007")
        FileUtils.touch("/tmp/dupa_32745923/dupa006")
        FileUtils.touch("/tmp/dupa_32745923/dupa005")
        FileUtils.touch("/tmp/dupa_32745923/dupa004")
        FileUtils.touch("/tmp/dupa_32745923/dupa003")
        SvdUtils.chown("/tmp/dupa_32745923", user = 123, group = 234, recursive = true)
        
        0 must beEqual(0)
    }
    
    
}
