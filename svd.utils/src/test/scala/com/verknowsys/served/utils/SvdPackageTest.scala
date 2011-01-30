package com.verknowsys.served.utils

import org.specs._

class SvdPackageTest extends Specification {
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
    
    "Path builder" should {
        "work" in {
            "foo"  / "bar"  must_== "foo/bar"
            "foo/" / "bar"  must_== "foo/bar"
            "foo"  / "/bar" must_== "foo/bar"
            "foo/" / "/bar" must_== "foo/bar"
            
            "foo/bar"  / "baz"  must_== "foo/bar/baz"
            "foo/bar/" / "baz"  must_== "foo/bar/baz"
            "foo/bar"  / "/baz" must_== "foo/bar/baz"
            "foo/bar/" / "/baz" must_== "foo/bar/baz"
            
            "/abs/foo/bar"  / "baz"  must_== "/abs/foo/bar/baz"
            "/abs/foo/bar/" / "baz"  must_== "/abs/foo/bar/baz"
            "/abs/foo/bar"  / "/baz" must_== "/abs/foo/bar/baz"
            "/abs/foo/bar/" / "/baz" must_== "/abs/foo/bar/baz"
            
            "/abs/foo/bar"  / "baz/blah"  must_== "/abs/foo/bar/baz/blah"
            "/abs/foo/bar/" / "baz/blah"  must_== "/abs/foo/bar/baz/blah"
            "/abs/foo/bar"  / "/baz/blah" must_== "/abs/foo/bar/baz/blah"
            "/abs/foo/bar/" / "/baz/blah" must_== "/abs/foo/bar/baz/blah"
        }
    }
}
