package com.verknowsys.served.systemmanager


import com.verknowsys.served.SvdConfig
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.events.SvdFileEvent
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.systemmanager.managers._
import com.verknowsys.served.api._

import akka.actor.{Actor, ActorRef}
import akka.actor.Actor.{remote, actorOf, registry}
import scala.io.Source
import java.io.File


case class GetAccountManager(username: String)


class SvdAccountsManager extends Actor with SvdFileEventsReactor with SvdExceptionHandler with Logging {
    
    import events._
    
    log.info("SvdAccountsManager is loading")
    
    // case object ReloadUsers
    // case class CheckUser(val username: String)
    
    // protected val systemPasswdFilePath = SvdConfig.systemPasswdFile // NOTE: This must be copied into value to use in pattern matching
    
    // Safe map for fast access to AccountManagers
    protected var accountManagers: Option[Map[String, ActorRef]] = None


    def receive = {
        case Init =>
            log.debug("SvdAccountsManager received Init. Running default task..")
            // registerFileEventFor(SvdConfig.systemHomeDir, Modified)
            respawnUsersActors
            self reply_? Success
        
        // 2011-06-22 01:34:12 - dmilith - PENDING: FIXME: TODO: fix file watcher issues
        // case SvdFileEvent(systemPasswdFilePath, Modified) => 
            // log.trace("Passwd file modified")
            // respawnUsersActors
            
        case GetAccountManager(username) =>
            self reply accountManagers.flatMap(_.get(username))
        
        case x: Any =>
            log.warn("%s has received unknown signal: %s".format(this.getClass, x))
            
    }

    private def respawnUsersActors {
        // // kill all Account Managers
        // log.trace("Actor.registry size before: %d", registry.actors.size)
        // registry.actorsFor[SvdAccountManager] foreach { _.stop }
        // 
        // // spawn Account Manager for each account entry in passwd file
        // accountManagers = Some(userAccounts.map { account =>
        //     val manager = actorOf(new SvdAccountManager(account))
        //     self.link(manager)
        //     manager.start
        //     (account.userName, manager)
        // }.toMap)
        // log.trace("Actor.registry size after: %d", registry.actors.size)
        
        val wrapper = SvdWrapLibrary.instance
        userAccounts.foreach{
            ua =>
                log.warn("Sending spawn message for account: %s".format(ua))
                wrapper.sendSpawnMessage(ua.uid.toString)
                
        }
        
        val map = Map( // 2011-06-22 16:49:20 - dmilith - XXX: FIXME: TODO: merge it into SvdAccount (gather automatically)
            "teamon" -> ("localhost", 8000),
            "dmilith" -> ("localhost", 8000)
        )
        
        accountManagers = Some(map.mapValues { case(host, port) =>
            val am = remote.actorFor("service:account-manager", host, port)
            // self link am
            am
        })
    }

    
    /**
     * Function to read user accounts from filesystem
     * @author dmilith
     */
    protected def userAccounts = {
        val shd = new File(SvdConfig.systemHomeDir)
        if (shd.exists) {
            val systemHomeDir = shd.listFiles.toList
            val accounts = systemHomeDir.map{
                hd =>
                    log.warn("HD: %s".format(hd))
                    new SvdAccount(uid = hd.getPath.split("/").reverse.head.toInt)
            }
            log.debug("allAccounts(), systemHomeDir: %s".format(systemHomeDir))
            accounts
        } else {
            new File(SvdConfig.systemHomeDir).mkdirs
            log.trace("Created %s. No users in system!".format(SvdConfig.systemHomeDir))
            
            new File(SvdConfig.systemHomeDir / "501") // 2011-06-22 17:00:15 - dmilith - HACK: HARDCODE: XXX: FIXME: auto "add" default user
            
            Nil
        }
    }
     
     
}
