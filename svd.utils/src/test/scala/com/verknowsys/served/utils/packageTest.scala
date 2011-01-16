package com.verknowsys.served.utils

import org.specs._

class PackageTest extends Specification {
    "String replace" should {
        "replace by name" in {
            "%{a} + %{b} = %{c}" % ("a" -> "1", "b" -> "2", "c" -> "3") must beEqual("1 + 2 = 3")
            "%{b} + %{a} = %{c}" % ("a" -> "1", "b" -> "2", "c" -> "3") must beEqual("2 + 1 = 3")
        }

        "replace by order" in {
            "% + % = %" % (1,2,3) must beEqual("1 + 2 = 3")
            "% + % = %" % (2,1,3) must beEqual("2 + 1 = 3")
        }
    }
}
