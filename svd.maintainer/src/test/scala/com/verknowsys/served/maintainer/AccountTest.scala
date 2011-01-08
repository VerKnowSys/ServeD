package com.verknowsys.served.maintainer

import org.specs._

class AccountTest extends Specification {
    "Account" should {
        
        "be equal" in {
            val a1 = Account("aa", "xx", "1000", "2000", "foo bar", "/home/aa", "/path/to/bash")
            val a2 = Account("aa", "yy", "1000", "2000", "foo foo", "/home/aax", "/path/to/zsh")
            val b = Account("bb", "zz", "2000", "2000", "foo bar baz", "/home/bb", "/path/to/ksh")
            val c = Account("cc", "zz", "2000", "2000", "foo bar blah", "/home/cc", "/path/to/sh")

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
            Account("", "", "", "", "", "/home/teamon", "/path/to/shell").isUser must beTrue
            Account("", "", "", "", "", "/home/foo", "/path/to/shell").isUser must beTrue
            Account("", "", "", "", "", "/foo", "/path/to/shell").isUser must beFalse
            Account("", "", "", "", "", "/foo/home", "/path/to/shell").isUser must beFalse
            Account("", "", "", "", "", "/foo/home/bar", "/path/to/shell").isUser must beFalse
        }
        
        "correctly parse passwd line" in {
            val Account(account) = "teamon:pass:myUid:myGid:info:/path/to/home:/path/to/shell"
            
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
