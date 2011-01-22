package com.verknowsys.served.maintainer

import com.verknowsys.served.utils.SvdFileEventsManager
import com.verknowsys.served.SvdSpecHelpers._
import com.verknowsys.served.spechelpers._
import com.verknowsys.served.SvdConfig
import com.verknowsys.served.managers._
import org.specs._

import akka.actor._
import akka.actor.Actor._


class SvdAccountsManagerTest extends Specification with SvdExpectActorSpecification {
    def changePasswdPath(path: String) {
        val passwd = readFile(System.getProperty("user.dir") + "/svd.maintainer/src/test/resources/etc/" + path)
        writeFile(SvdConfig.systemPasswdFile, passwd)
    }      
    
    def waitForKqueue = waitFor(500) // 2011-01-22 17:41:34 - dmilith - XXX: hardcode
    
    var am: ActorRef = null
    var fem: ActorRef = null
    
    "SvdAccountsManager" should {
        doBefore {
            beforeExpectActor
            fem = actorOf[SvdFileEventsManager].start
        }
        
        doAfter { 
            afterExpectActor
            registry.shutdownAll 
        }
        
        "not spawn any Account Managers" in {
            changePasswdPath("emptyPasswd")
            
            registry.actorsFor[SvdAccountManager] must haveSize(0)
            am = actorOf[SvdAccountsManager].start
            registry.actorsFor[SvdAccountManager] must haveSize(0)
        }
        
        "spawn one Account Manager" in {
            changePasswdPath("standardPasswd")
            
            registry.actorsFor[SvdAccountManager] must haveSize(0)
            am = actorOf[SvdAccountsManager].start
            
            val managers = registry.actorsFor[SvdAccountManager]
            managers must haveSize(1)
            managers.map(a => (a !! GetAccount).get) must contain(SvdAccount("teamon", "*", "1001", "1001", "User &", "/home/teamon", "/usr/local/bin/zsh"))
        }
        
        "spawn few Account Managers" in {
            changePasswdPath("fivePasswd")
            
            registry.actorsFor[SvdAccountManager] must haveSize(0)
            am = actorOf[SvdAccountsManager].start
            
            val managers = registry.actorsFor[SvdAccountManager]
            managers must haveSize(5)
            val accounts = managers.map(a => (a !! GetAccount)).collect {
                case Some(a: SvdAccount) => a
            }
            
            accounts.map(_.userName) must containAll("teamon" :: "dmilith" :: "foo" :: "bar" :: "baz" :: Nil)
        }
     }
}
