package com.verknowsys.served.maintainer

import com.verknowsys.served.utils.FileEventsManager
import com.verknowsys.served.SpecHelpers._
import com.verknowsys.served.spechelpers._
import com.verknowsys.served.Config
import com.verknowsys.served.managers._
import org.specs._


import akka.actor._
import akka.actor.Actor._

class AccountsManagerTest extends Specification with ExpectActorSpecification {
    def changePasswdPath(path: String) {
        val passwd = readFile(System.getProperty("user.dir") + "/svd.maintainer/src/test/resources/etc/" + path)
        writeFile(Config.systemPasswdFile, passwd)
    }      
    
    def waitForKqueue = waitFor(500)
    
    var am: ActorRef = null
    var fem: ActorRef = null
    
    "AccountsManager" should {
        doBefore { 
            beforeExpectActor
            fem = actorOf[FileEventsManager].start
        }
        
        doAfter { 
            afterExpectActor
            registry.shutdownAll 
        }
        
        "not spawn any Account Managers" in {
            changePasswdPath("emptyPasswd")
            
            registry.actorsFor[AccountManager] must haveSize(0)
            am = actorOf[AccountsManager].start
            registry.actorsFor[AccountManager] must haveSize(0)
        }
        
        "spawn one Account Manager" in {
            changePasswdPath("standardPasswd")
            
            registry.actorsFor[AccountManager] must haveSize(0)
            am = actorOf[AccountsManager].start
            
            val managers = registry.actorsFor[AccountManager]
            managers must haveSize(1)
            managers.map(a => (a !! GetAccount).get) must contain(Account("teamon", "*", "1001", "1001", "User &", "/home/teamon", "/usr/local/bin/zsh"))
        }
        
        "spawn few Account Managers" in {
            changePasswdPath("fivePasswd")
            
            registry.actorsFor[AccountManager] must haveSize(0)
            am = actorOf[AccountsManager].start
            
            val managers = registry.actorsFor[AccountManager]
            managers must haveSize(5)
            val accounts = managers.map(a => (a !! GetAccount)).collect {
                case Some(a: Account) => a
            }
            
            accounts.map(_.userName) must containAll("teamon" :: "dmilith" :: "foo" :: "bar" :: "baz" :: Nil)
        }
     }
}