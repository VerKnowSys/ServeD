package com.verknowsys.served.maintainer

import com.verknowsys.served.utils.signals._
import com.verknowsys.served.Config
import org.specs._
import org.apache.commons.io.FileUtils
import java.io.File
import scala.actors.Actor

object Impl { implicit def StringToFile(s: String) = new File(s) }
import Impl._


class AccountsManagerTest extends SpecificationWithJUnit {   
    import Impl._
     
    def waitFor(actor: Actor) {
        println("Waiting for " + actor)
        var time = 0
        while(actor.getState != Actor.State.Blocked) { time+=1; Thread.sleep(50) }
        println("Waited " + time*50 + "ms")
    }
    
    def waitFor(time: Int) {
        println("Waiting for " + time + "ms")
        Thread.sleep(time)
    }

    def changePasswdPath(path: String){
        val passwd = FileUtils.readFileToString(System.getProperty("user.dir") + "/src/test/resources/etc/" + path)
        FileUtils.writeStringToFile(Config.systemPasswdFile, passwd)
    }

    "AccountsManager" should {
        "create manager for each account" in {            
            AccountsManager ! Init
            waitFor(AccountsManager)
            
            changePasswdPath("emptyPasswd")
            waitFor(10000) // kqueue delay
            waitFor(AccountsManager)
            
            AccountsManager.managers must beEmpty
            
            
            changePasswdPath("standardPasswd")
            waitFor(10000) // kqueue delay
            waitFor(AccountsManager)
            
            val res = AccountsManager.managers
            res must haveSize(1)
            val account = res(0).account 
            account must beEqual(Account("teamon", "*", "1001", "1001", "User &", "/home/teamon", "/usr/local/bin/zsh"))
            account.userName must beEqual("teamon")
            account.pass must beEqual("*")
            account.uid must beEqual("1001")
            account.gid must beEqual("1001")
            account.information must beEqual("User &")
            account.homeDir must beEqual("/home/teamon")
            account.shell must beEqual("/usr/local/bin/zsh")
            

            // AccountsManager ! Quit
        }
    }
    
}