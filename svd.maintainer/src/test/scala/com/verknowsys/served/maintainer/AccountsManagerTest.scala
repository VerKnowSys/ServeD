package com.verknowsys.served.maintainer

import com.verknowsys.served.utils.signals._
import com.verknowsys.served.SpecHelpers._
import com.verknowsys.served.Config
import org.specs._
import scala.actors.Actor

import com.verknowsys.served.utils.monitor.Monitor


class AccountsManagerTest extends SpecificationWithJUnit {
    "AccountsManager" should {
        "create manager for each account" in {
            Monitor.start
            
            def changePasswdPath(path: String) {
                val passwd = readFile(System.getProperty("user.dir") + "/svd.maintainer/src/test/resources/etc/" + path)
                writeFile(Config.systemPasswdFile, passwd)
            }      
            
            def waitForKqueue = waitFor(500) 
            
            AccountsManager ! Init
            
            // No accounts
            changePasswdPath("emptyPasswd")
            waitForKqueue
            waitWhileRunning(AccountsManager)
            
            AccountsManager.managers must beEmpty
            
            // One account
            changePasswdPath("standardPasswd")
            waitForKqueue
            waitWhileRunning(AccountsManager)
            
            val res1 = AccountsManager.managers
            res1 must haveSize(1)
            val account = res1(0).account 
            account must beEqual(Account("teamon", "*", "1001", "1001", "User &", "/home/teamon", "/usr/local/bin/zsh"))
            account.userName must beEqual("teamon")
            account.pass must beEqual("*")
            account.uid must beEqual("1001")
            account.gid must beEqual("1001")
            account.information must beEqual("User &")
            account.homeDir must beEqual("/home/teamon")
            account.shell must beEqual("/usr/local/bin/zsh")
            
            changePasswdPath("fivePasswd")
            waitForKqueue
            waitWhileRunning(AccountsManager)
            val res2 = AccountsManager.managers
            val users = res2.map(_.account.userName)
            users must haveSize(5)
            users must containAll("teamon" :: "dmilith" :: "foo" :: "bar" :: "baz" :: Nil)
            

            AccountsManager ! Quit
            waitForDeath(AccountsManager)
        }
    }
    
}