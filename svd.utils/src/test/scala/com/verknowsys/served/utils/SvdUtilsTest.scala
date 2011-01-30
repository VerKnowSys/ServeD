package com.verknowsys.served.utils

import com.verknowsys.served.SvdSpecHelpers._
import org.specs._

class SvdUtilsTest extends Specification {
    "SvdUtils#fileExists" should {
        "return true if file exists" in {
            mkdir("/tmp/served/utils_test/foo")
            SvdUtils.fileExists("/tmp/served/utils_test/foo") must beTrue
        }

        "return false if file does not exist" in {
            rmdir("/tmp/served/utils_test")
            SvdUtils.fileExists("/tmp/served/utils_test/foo") must beFalse
        }
    }
}
