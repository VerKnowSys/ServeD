package com.verknowsys.served.maintainer

import org.specs._

class SvdAccountTest extends Specification {
    "SvdAccount" should {
        
        "provide default parameters" in {
            val a = new SvdAccount()
            
            a.userName must beEqual("guest")
            a.pass must beEqual("x")
            a.uid must beEqual("1000")
            a.gid must beEqual("1000")
            a.information must beEqual("No information")
            a.homeDir must beEqual("/home/")
            a.shell must beEqual("/bin/bash")
        }
        
        "be equal" in {
            val a1 = SvdAccount("aa", "xx", "1000", "2000", "foo bar", "/home/aa", "/path/to/bash")
            val a2 = SvdAccount("aa", "yy", "1000", "2000", "foo foo", "/home/aax", "/path/to/zsh")
            val b = SvdAccount("bb", "zz", "2000", "2000", "foo bar baz", "/home/bb", "/path/to/ksh")
            val c = SvdAccount("cc", "zz", "2000", "2000", "foo bar blah", "/home/cc", "/path/to/sh")

            a1 must beEqual(a1);        a1.hashCode must beEqual(a1.hashCode)
            a1 must beEqual(a2);        a1.hashCode must beEqual(a2.hashCode)
            a1 must beDifferent(b);     a1.hashCode must beDifferent(b.hashCode)
            a1 must beDifferent(c);     a1.hashCode must beDifferent(c.hashCode)

            a2 must beEqual(a1);        a2.hashCode must beEqual(a1.hashCode)
            a2 must beEqual(a2);        a2.hashCode must beEqual(a2.hashCode)
            a2 must beDifferent(b);     a2.hashCode must beDifferent(b.hashCode)
            a2 must beDifferent(c);     a2.hashCode must beDifferent(c.hashCode)

            b must beDifferent(a1);     b.hashCode must beDifferent(a1.hashCode)
            b must beDifferent(a2);     b.hashCode must beDifferent(a2.hashCode)
            b must beEqual(b);          b.hashCode must beEqual(b.hashCode)
            b must beDifferent(c);      b.hashCode must beDifferent(c.hashCode)

            c must beDifferent(a1);     c.hashCode must beDifferent(a1.hashCode)
            c must beDifferent(a2);     c.hashCode must beDifferent(a2.hashCode)
            c must beDifferent(b);      c.hashCode must beDifferent(b.hashCode)
            c must beEqual(c);          c.hashCode must beEqual(c.hashCode)
        }
        
        "isUser" in {
            SvdAccount("", "", "", "", "", "/home/teamon", "/path/to/shell").isUser must beTrue
            SvdAccount("", "", "", "", "", "/home/foo", "/path/to/shell").isUser must beTrue
            SvdAccount("", "", "", "", "", "/foo", "/path/to/shell").isUser must beFalse
            SvdAccount("", "", "", "", "", "/foo/home", "/path/to/shell").isUser must beFalse
            SvdAccount("", "", "", "", "", "/foo/home/bar", "/path/to/shell").isUser must beFalse
        }
        
        "correctly parse passwd line" in {
            val SvdAccount(account) = "teamon:pass:myUid:myGid:info:/path/to/home:/path/to/shell"
            
            account.userName must beEqual("teamon")
            account.pass must beEqual("pass")
            account.uid must beEqual("myUid")
            account.gid must beEqual("myGid")
            account.information must beEqual("info")
            account.homeDir must beEqual("/path/to/home")
            account.shell must beEqual("/path/to/shell")
        }
        
    }

}
