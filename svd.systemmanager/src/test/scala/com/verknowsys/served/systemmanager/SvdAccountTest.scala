package com.verknowsys.served.systemmanager


import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.systemmanager.acl._

import org.specs._
import akka.util.Logging


class SvdAccountTest extends Specification with Logging {

    "SvdAccount" should {
        
        import SvdACL._
        
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
            val a1 = SvdAccount("aa", "xx", 1000, 2000, "foo bar", "/home/aa", "/path/to/bash", Nil)
            val a2 = SvdAccount("aa", "yy", 1000, 2000, "foo foo", "/home/aax", "/path/to/zsh", Nil)
            val b = SvdAccount("bb", "zz", 2000, 2000, "foo bar baz", "/home/bb", "/path/to/ksh", Nil)
            val c = SvdAccount("cc", "zz", 2000, 2000, "foo bar blah", "/home/cc", "/path/to/sh", Nil)

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
            SvdAccount("", "", 1, 1, "", "/home/teamon", "/path/to/shell", Nil).isUser must beTrue
            SvdAccount("", "", 1, 1, "", "/home/foo", "/path/to/shell", Nil).isUser must beTrue
            SvdAccount("", "", 1, 1, "", "/foo", "/path/to/shell", Nil).isUser must beFalse
            SvdAccount("", "", 1, 1, "", "/foo/home", "/path/to/shell", Nil).isUser must beFalse
            SvdAccount("", "", 1, 1, "", "/foo/home/bar", "/path/to/shell", Nil).isUser must beFalse
        }
        
        "correctly parse ordered arguments" in {
            val account = new SvdAccount("teamon", "pass", 1001, 1002, "info", "/path/to/home", "/path/to/shell", RootAllowed :: Nil )
            
            account.userName must beEqual("teamon")
            account.pass must beEqual("pass")
            account.uid must beEqual(1001)
            account.gid must beEqual(1002)
            account.information must beEqual("info")
            account.homeDir must beEqual("/path/to/home")
            account.shell must beEqual("/path/to/shell")
            account.acls match {
                case (RootAllowed :: Nil) =>
                    log.info("Root Allowed.")
                    
                case _ =>
                    fail("ACLS didn't pass our test.")
            }
        }
        
        "chown should obey SvdACL implementation" in {
            val acl1 = ExecutionAllowed("/bin/ls" :: "/bin/du" :: Nil) :: RootAllowed :: Nil
            val acl2 = ExecutionAllowed("uname" :: Nil) :: Nil
            val acl3 = OneProcAllowed("memcached") :: SSHAllowed :: Nil
            
            acl1 match {
                case ExecutionAllowed(xx :: yy :: Nil) :: RootAllowed :: Nil =>
                    xx must beEqual("/bin/ls")
                    yy must beEqual("/bin/du")
                case x => 
                    fail("acl1 should be like expected. Exc: %s".format(x))
            }
            acl2 match {
                case ExecutionAllowed(xx :: Nil) :: Nil =>
                    xx must beEqual("uname")
                case _ =>
                    fail("acl2 should be like expected")
            }
            acl3 match {
                case OneProcAllowed(name) :: SSHAllowed :: Nil =>
                    name must notBeNull
                    name must beEqual("memcached")
                case _ =>
                    fail("acl3a should be like expected")
            }
            acl3 match {
                case xx :: SSHAllowed :: Nil =>
                    xx must beEqual(OneProcAllowed("memcached"))
                case _ =>
                    fail("acl3b should be like expected")
            }
            
        }
        
        
        "generation of new copy of account data should be simple" in {
            val account = new SvdAccount("teamon", "pass", 1001, 1002, "info", "/path/to/home", "/path/to/shell", SSHAllowed :: Nil )
            val acc2 = account copy(pass = "lalala666")
            "lalala666" must beEqual(acc2.pass)
            account.userName must beEqual(acc2.userName)
            account.uid must beEqual(acc2.uid)
            account.gid must beEqual(acc2.gid)
            account.information must beEqual(acc2.information)
            account.homeDir must beEqual(acc2.homeDir)
            account.shell must beEqual(acc2.shell)
            account.acls must beEqual(acc2.acls)

            var res = false
            account.acls match {
                case RootAllowed :: Nil =>
                    fail("Root allow found where no RootAllowed!")
                case SSHAllowed :: Nil =>
                    res = true
                case _ =>
                    fail("ACLS didn't pass our test.")
            }
            res must beTrue
        }
        
    }

}
