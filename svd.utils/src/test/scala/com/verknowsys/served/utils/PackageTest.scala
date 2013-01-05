/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.utils

import com.verknowsys.served.testing._

class StringReplaceTest extends DefaultTest {
    it should "replace by name" in {
        "%{a} + %{b} = %{c}" % ("a" -> "1", "b" -> "2", "c" -> "3") should be ("1 + 2 = 3")
        "%{b} + %{a} = %{c}" % ("a" -> "1", "b" -> "2", "c" -> "3") should be ("2 + 1 = 3")
    }
    
    it should "replace by order" in {
        "% + % = %" % (1,2,3) should be ("1 + 2 = 3")
        "% + % = %" % (2,1,3) should be ("2 + 1 = 3")
    }
}

class PathBuilderSpec extends DefaultTest {
    it should "join with empty path" in {
        (""  / "bar") should be ("bar")
        ("foo"  / "") should be ("foo")
        (""  / "")    should be ("")
    }
    
    it should "join with slash" in {
        "foo"  / "bar"  should be ("foo/bar")
        "foo/" / "bar"  should be ("foo/bar")
        "foo"  / "/bar" should be ("foo/bar")
        "foo/" / "/bar" should be ("foo/bar")
    }
    
    it should "join absolute with slash" in {
        "/abs/foo/bar"  / "baz"  should be ("/abs/foo/bar/baz")
        "/abs/foo/bar/" / "baz"  should be ("/abs/foo/bar/baz")
        "/abs/foo/bar"  / "/baz" should be ("/abs/foo/bar/baz")
        "/abs/foo/bar/" / "/baz" should be ("/abs/foo/bar/baz")
    }
    
    it should "join absolute and complex with slash" in {
        "/abs/foo/bar"  / "baz/blah"  should be ("/abs/foo/bar/baz/blah")
        "/abs/foo/bar/" / "baz/blah"  should be ("/abs/foo/bar/baz/blah")
        "/abs/foo/bar"  / "/baz/blah" should be ("/abs/foo/bar/baz/blah")
        "/abs/foo/bar/" / "/baz/blah" should be ("/abs/foo/bar/baz/blah")
    }
}
