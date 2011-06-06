package com.verknowsys.served.systemmanager


import com.verknowsys.served.SvdConfig
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.utils.events.SvdFileEvent
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.systemmanager.managers._
import com.verknowsys.served.api._

import akka.actor.{Actor, ActorRef}
import akka.actor.Actor.{actorOf, registry}
import scala.io.Source



case class GetAccountManager(username: String)


class SvdAccountsManager extends Actor with SvdFileEventsReactor with SvdExceptionHandler {
    import events._
    
    log.info("SvdAccountsManager is loading")
    
    // case object ReloadUsers
    // case class CheckUser(val username: String)
    
    val systemPasswdFilePath = SvdConfig.systemPasswdFile // NOTE: This must be copied into value to use in pattern matching

    def receive = {
        case Init =>
            log.debug("SvdAccountsManager received Init. Running default task..")
            registerFileEventFor(SvdConfig.systemPasswdFile, Modified)
            respawnUsersActors
        
        case SvdFileEvent(systemPasswdFilePath, Modified) => 
            log.trace("Passwd file modified")
            respawnUsersActors
            
        case GetAccountManager(username) =>
            registry.actorsFor[SvdAccountManager].find { e => 
                (e !! GetAccount) collect { case a: SvdAccount => a.userName == username } getOrElse false 
            } match {
                case Some(ref: ActorRef) => self reply ref
                case _ => self reply Error("AccountManeger for username %s not found".format(username))
            }
    }

    private def respawnUsersActors {
        // kill all Account Managers
        log.trace("Actor.registry size before: %d", registry.actors.size)
        registry.actorsFor[SvdAccountManager] foreach { _.stop }
        
        // spawn Account Manager for each account entry in passwd file
        userAccounts foreach { account =>
            val manager = actorOf(new SvdAccountManager(account))
            self.link(manager)
            manager.start
        }
        log.trace("Actor.registry size after: %d", registry.actors.size)
    }

    
    /**
     * Function to parse and convert passwd file entries to List[Account]
     * @author teamon
     */
    protected def allAccounts = {
        val rawData = Source.fromFile(SvdConfig.systemPasswdFile, SvdConfig.defaultEncoding).getLines.toList
        for(SvdAccount(account) <- rawData) yield account
    }
     
     
    /**
     * Returns only normal users' accounts
     * @author teamon
     */
    protected def userAccounts = allAccounts.filter(_.isUser)

    
}
