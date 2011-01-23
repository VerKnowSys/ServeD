package com.verknowsys.served.maintainer

import scala.io.Source
import com.verknowsys.served.SvdConfig
import com.verknowsys.served.utils.SvdFileEventsReactor
import com.verknowsys.served.utils.events.SvdFileEvent
import com.verknowsys.served.managers._

import akka.actor.{Actor, ActorRef}
import akka.actor.Actor.{actorOf, registry}
import akka.util.Logging

import com.verknowsys.served.api._


case class GetAccountManager(username: String)

class SvdAccountsManager extends Actor with SvdFileEventsReactor {
    log.info("SvdAccountsManager is loading")
    // case object ReloadUsers
    // case class CheckUser(val username: String)
    
    override def preStart {
        respawnUsersActors
        registerFileEventFor(SvdConfig.systemPasswdFile, Modified)
    }
        
    def receive = {
        case SvdFileEvent(path, Modified) => 
            log.trace("Passwd file modified")
            respawnUsersActors
            
        case GetAccountManager(username) =>
            registry.actorsFor[SvdAccountManager].find { e => 
                (e !! GetAccount) collect { case a: SvdAccount => a.userName == username } getOrElse false 
            } match {
                case Some(ref: ActorRef) => self reply ref
                case _ => self reply Error("AccountManeger for username %s not found".format(username))
            }
                        
        case msg => log.warn("Message not recognized: %s", msg)
    }
        
    // def act {
    //     // TODO: Catch java.io.FileNotFoundException and exit. ServeD can`t run withour passwd file
    //     val watchPasswdFile = SvdKqueue.watch(SvdConfig.systemPasswdFile, modified = true) {
    //         trace("Triggered (modified/created) system password file: %s".format(SvdConfig.systemPasswdFile))
    //         SvdAccountsManager ! ReloadUsers
    //     }    
    //     
    //     trace("Initialized watch for " + SvdConfig.systemPasswdFile)
    //     trace("watchPasswordFile: " + watchPasswdFile)
    //     
    //     loop {
    //         receive {
    //             case Init =>
    //                 SvdAccountsManager ! ReloadUsers            
    //                 info("SvdAccountsManager ready")
    //                 
    //             case Quit =>
    //                 info("Quitting SvdAccountsManager")
    //                 watchPasswdFile.stop
    //                 exit
    //                 
    //             case ReloadUsers =>
    //                 trace("Reloading users list")
    //                 // How does it work
    //                 //
    //                 // <managers> = ListBuffer[SvdAccountManager]
    //                 // 1. Start
    //                 //  - loadSvdAccounts from /etc/passwd
    //                 //  - for each SvdAccount spawn SvdAccountManager and add it to <managers>
    //                 //  - setup SvdFileWatcher on /etc/passwd
    //                 //
    //                 // 2. /etc/passwd changed
    //                 // - read SvdAccounts list form /etc/passwd
    //                 // - for every SvdAccount 
    //                 //   - check if there is existing SvdAccountManager
    //                 //     if not, spawn SvdAccountManager and add it to <managers>
    //                 // - for every SvdAccountManager from <managers>
    //                 //   - check if it`s SvdAccount is in new accounts list
    //                 //     - if not, send Quit to manager and remove it from list
    //                 //
    //                 // TODO: Update user`s shell path
    //                 
    //                 val accounts = userSvdAccounts
    //                                                         
    //                 accounts foreach { a =>
    //                     if(!managers.exists(_.account == a)) managers += new SvdAccountManager(a)
    //                 }
    //                 
    //                 managers.foreach { m =>
    //                     if(!accounts.exists(_ == m.account)){
    //                         m ! Quit
    //                         managers -= m
    //                     }
    //                 }
    //                 
    //             case CheckUser(username) =>
    //                 // trace("Checking if user %s exist" % username)
    //                 reply(managers.find(_.account.userName == username))
    //             
    //             case _ => messageNotRecognized(_)
    //                 
    //         }
    //     }
    // }


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
