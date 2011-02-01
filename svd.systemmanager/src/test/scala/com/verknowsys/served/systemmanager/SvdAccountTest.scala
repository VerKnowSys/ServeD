package com.verknowsys.served.systemmanager


import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.systemmanager.acl._

import org.specs._


class SvdAccountTest extends Specification {

    "SvdAccount" should {
        
        "provide default parameters" in {
            val a = new SvdAccount()
            
            a.userName must beEqual("guest")
            a.pass must beEqual("x")
            a.uid must beEqual(1000)
            a.gid must beEqual(1000)
            a.information must beEqual("No information")
            a.homeDir must beEqual("/home/")
            a.shell must beEqual("/bin/bash")
        }
        
        "be equal" in {
            val a1 = SvdAccount("aa", "xx", 1000, 2000, "foo bar", "/home/aa", "/path/to/bash", null)
            val a2 = SvdAccount("aa", "yy", 1000, 2000, "foo foo", "/home/aax", "/path/to/zsh", null)
            val b = SvdAccount("bb", "zz", 2000, 2000, "foo bar baz", "/home/bb", "/path/to/ksh", null)
            val c = SvdAccount("cc", "zz", 2000, 2000, "foo bar blah", "/home/cc", "/path/to/sh", null)

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
            SvdAccount("", "", 1, 1, "", "/home/teamon", "/path/to/shell", null).isUser must beTrue
            SvdAccount("", "", 1, 1, "", "/home/foo", "/path/to/shell", null).isUser must beTrue
            SvdAccount("", "", 1, 1, "", "/foo", "/path/to/shell", null).isUser must beFalse
            SvdAccount("", "", 1, 1, "", "/foo/home", "/path/to/shell", null).isUser must beFalse
            SvdAccount("", "", 1, 1, "", "/foo/home/bar", "/path/to/shell", null).isUser must beFalse
        }
        
        "correctly parse passwd line" in {
            val SvdAccount(account) = "teamon:pass:1001:1002:info:/path/to/home:/path/to/shell"
            
            account.userName must beEqual("teamon")
            account.pass must beEqual("pass")
            account.uid must beEqual(1001)
            account.gid must beEqual(1002)
            account.information must beEqual("info")
            account.homeDir must beEqual("/path/to/home")
            account.shell must beEqual("/path/to/shell")
        }
        
        "chown should obey SvdACL implementation" in {
            val acl1 = new SvdACL with ExecutionAllowed with RootAllowed
            val acl2 = new SvdACL with ExecutionAllowed
            val acl3 = new SvdACL with OneProcAllowed with SSHAllowed
            
            acl1 must beLike {
                case x: ExecutionAllowed =>
                    true
                case _ =>
                    fail("acl1 should beLike expected")
            }
            acl1 must beLike {
                case x: RootAllowed =>
                    true
                case _ =>
                    fail("acl1 should beLike expected")
            }
            
            acl2 must beLike {
                case x: ExecutionAllowed =>
                    true
                case _ =>
                    fail("acl2 should beLike expected")
            }

            acl3 must beLike {
                case x: OneProcAllowed =>
                    true
                case _ =>
                    fail("acl3 should beLike expected")
            }
            acl3 must beLike {
                case x: SSHAllowed =>
                    true
                case _ =>
                    fail("acl3 should beLike expected")
            }
            
        }
        
    }

}
