package com.verknowsys.served.utils

import org.specs2._

class StringReplaceSpec extends Specification { def is =
    
    "String replace specification"      ^
                                        p^
    "Replacing by name"                 ^
        "replace with order"            ! e1^
        "replace without order"         ! e2^
                                        p^
    "Replacing by order"                ^
        "first test"                    ! e3^
        "second test"                   ! e4^
                                        end
                                        
    def e1 = "%{a} + %{b} = %{c}" % ("a" -> "1", "b" -> "2", "c" -> "3") must_== "1 + 2 = 3"
    def e2 = "%{b} + %{a} = %{c}" % ("a" -> "1", "b" -> "2", "c" -> "3") must_== "2 + 1 = 3"
    def e3 = "% + % = %" % (1,2,3) must_== "1 + 2 = 3"
    def e4 = "% + % = %" % (2,1,3) must_== "2 + 1 = 3"
}

class PathBuilderSpec extends Specification { def is = 

    "Path builder specification"                    ^ 
                                                    p^
    "Joining with empty path"                       ^ 
        "empty A + B = B"                           ! { (""  / "bar")  must_== "bar" }^
        "A + empty B = A"                           ! { ("foo"  / "")  must_== "foo" }^
        "empty A + empty B = empty"                 ! { (""  / "")  must_== "" }^
                                                    p^
    "Joining with slash"                            ^
        "A + B = A/B"                               ! { "foo"  / "bar"  must_== "foo/bar" }^
        "A/ + B = A/B"                              ! { "foo/" / "bar"  must_== "foo/bar" }^
        "A + /B = A/B"                              ! { "foo"  / "/bar" must_== "foo/bar" }^
        "A/ + /B = A/B"                             ! { "foo/" / "/bar" must_== "foo/bar" }^
                                                    p^
    "Joining absolute with slash"                   ^
        "/A + B = /A/B"                             ! { "/abs/foo/bar"  / "baz"  must_== "/abs/foo/bar/baz" }^
        "/A/ + B = /A/B"                            ! { "/abs/foo/bar/" / "baz"  must_== "/abs/foo/bar/baz" }^
        "/A + /B = /A/B"                            ! { "/abs/foo/bar"  / "/baz" must_== "/abs/foo/bar/baz" }^
        "/A/ + /B = /A/B"                           ! { "/abs/foo/bar/" / "/baz" must_== "/abs/foo/bar/baz" }^
                                                    p^
    "Joining absolute and complex with slash"       ^
        "/A + B = /A/B"                             ! { "/abs/foo/bar"  / "baz/blah"  must_== "/abs/foo/bar/baz/blah" }^
        "/A/ + B = /A/B"                            ! { "/abs/foo/bar/" / "baz/blah"  must_== "/abs/foo/bar/baz/blah" }^
        "/A + /B = /A/B"                            ! { "/abs/foo/bar"  / "/baz/blah" must_== "/abs/foo/bar/baz/blah" }^
        "/A/ + /B = /A/B"                           ! { "/abs/foo/bar/" / "/baz/blah" must_== "/abs/foo/bar/baz/blah" }^                              
                                                    end
}
