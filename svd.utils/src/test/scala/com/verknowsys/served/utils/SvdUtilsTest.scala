package com.verknowsys.served.utils

import org.apache.commons.io.FileUtils
import com.verknowsys.served.SvdSpecHelpers._
import org.specs._


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
        SvdUtils.chown("/tmp/dupa007", 666, group = 6666)
        SvdUtils.chmod("/tmp/dupa007", 0777) // 2011-01-31 01:01:42 - dmilith -NOTE: it's in OCT. it's same as chmod 511
    }
    
}
