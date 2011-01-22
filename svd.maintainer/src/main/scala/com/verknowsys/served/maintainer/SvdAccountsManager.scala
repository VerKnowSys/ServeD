package com.verknowsys.served.maintainer

import scala.io.Source
import com.verknowsys.served.SvdConfig

import com.verknowsys.served.managers.SvdAccountSvdManager

import akka.actor.Actor
import akka.actor.Actor.actorOf
import akka.util.Logging


class SvdAccountsManager extends Actor with Logging {
    // case object ReloadUsers
    // case class CheckUser(val username: String)
    
    // start

    // val managers = new ListBuffer[SvdAccountSvdManager]
    
    spawnLinkUsersActors
    
        
    def receive = {
        case x => println("AM got " + x)
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
    //                 // <managers> = ListBuffer[SvdAccountSvdManager]
    //                 // 1. Start
    //                 //  - loadSvdAccounts from /etc/passwd
    //                 //  - for each SvdAccount spawn SvdAccountSvdManager and add it to <managers>
    //                 //  - setup SvdFileWatcher on /etc/passwd
    //                 //
    //                 // 2. /etc/passwd changed
    //                 // - read SvdAccounts list form /etc/passwd
    //                 // - for every SvdAccount 
    //                 //   - check if there is existing SvdAccountSvdManager
    //                 //     if not, spawn SvdAccountSvdManager and add it to <managers>
    //                 // - for every SvdAccountSvdManager from <managers>
    //                 //   - check if it`s SvdAccount is in new accounts list
    //                 //     - if not, send Quit to manager and remove it from list
    //                 //
    //                 // TODO: Update user`s shell path
    //                 
    //                 val accounts = userSvdAccounts
    //                                                         
    //                 accounts foreach { a =>
    //                     if(!managers.exists(_.account == a)) managers += new SvdAccountSvdManager(a)
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



    /**
     * Function to parse and convert List[String] of passwd file entries to List[SvdAccount]
     * @author dmilith
     */
    // protected def allSvdAccounts = {
    //     val rawData = Source.fromFile(SvdConfig.systemPasswdFile, "utf-8").getLines.toList
    //     for(line <- rawData if !line.startsWith("#")) // XXX: hardcode
    //         yield
    //             new SvdAccount(line.split(":").toList)
    // }
    
    private def spawnLinkUsersActors {
       userSvdAccounts foreach { account =>
           val manager = actorOf(new SvdAccountSvdManager(account))
           self.link(manager)
           manager.start
       }
    }
    
    /**
     * Function to parse and convert List[String] of passwd file entries to List[SvdAccount]
     * @author teamon
     */
    protected def allSvdAccounts = {
        val rawData = Source.fromFile(SvdConfig.systemPasswdFile, "utf-8").getLines.toList
        for(SvdAccount(account) <- rawData) yield account
    }
     
     
    /**
     * Returns only normal users' accounts
     * @author teamon
     */
    protected def userSvdAccounts = allSvdAccounts.filter(_.isUser)
    
}