package com.verknowsys.served.maintainer

// import scala.actors.Actor
import scala.io.Source
// import scala.collection.mutable.ListBuffer

import com.verknowsys.served.Config
// import com.verknowsys.served.utils.CommonActor
import com.verknowsys.served.utils.Logged
// import com.verknowsys.served.utils.monitor.Monitored
// import com.verknowsys.served.utils.kqueue.Kqueue
// import com.verknowsys.served.managers.AccountManager

// import scala.actors.Actor

import com.verknowsys.served.managers.AccountManager

import akka.actor.Actor
import akka.actor.Actor.actorOf


class AccountsManager extends Actor with Logged {
    // case object ReloadUsers
    // case class CheckUser(val username: String)
    
    // start

    // val managers = new ListBuffer[AccountManager]
    
    spawnLinkUsersActors
    
        
    def receive = {
        case x => println("AM got " + x)
    }
        
    // def act {
    //     // TODO: Catch java.io.FileNotFoundException and exit. ServeD can`t run withour passwd file
    //     val watchPasswdFile = Kqueue.watch(Config.systemPasswdFile, modified = true) {
    //         trace("Triggered (modified/created) system password file: %s".format(Config.systemPasswdFile))
    //         AccountsManager ! ReloadUsers
    //     }    
    //     
    //     trace("Initialized watch for " + Config.systemPasswdFile)
    //     trace("watchPasswordFile: " + watchPasswdFile)
    //     
    //     loop {
    //         receive {
    //             case Init =>
    //                 AccountsManager ! ReloadUsers            
    //                 info("AccountsManager ready")
    //                 
    //             case Quit =>
    //                 info("Quitting AccountsManager")
    //                 watchPasswdFile.stop
    //                 exit
    //                 
    //             case ReloadUsers =>
    //                 trace("Reloading users list")
    //                 // How does it work
    //                 //
    //                 // <managers> = ListBuffer[AccountManager]
    //                 // 1. Start
    //                 //  - loadAccounts from /etc/passwd
    //                 //  - for each Account spawn AccountManager and add it to <managers>
    //                 //  - setup FileWatcher on /etc/passwd
    //                 //
    //                 // 2. /etc/passwd changed
    //                 // - read Accounts list form /etc/passwd
    //                 // - for every Account 
    //                 //   - check if there is existing AccountManager
    //                 //     if not, spawn AccountManager and add it to <managers>
    //                 // - for every AccountManager from <managers>
    //                 //   - check if it`s Account is in new accounts list
    //                 //     - if not, send Quit to manager and remove it from list
    //                 //
    //                 // TODO: Update user`s shell path
    //                 
    //                 val accounts = userAccounts
    //                                                         
    //                 accounts foreach { a =>
    //                     if(!managers.exists(_.account == a)) managers += new AccountManager(a)
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
     * Function to parse and convert List[String] of passwd file entries to List[Account]
     * @author dmilith
     */
    // protected def allAccounts = {
    //     val rawData = Source.fromFile(Config.systemPasswdFile, "utf-8").getLines.toList
    //     for(line <- rawData if !line.startsWith("#")) // XXX: hardcode
    //         yield
    //             new Account(line.split(":").toList)
    // }
    
    private def spawnLinkUsersActors {
       userAccounts foreach { account =>
           val manager = actorOf(new AccountManager(account))
           self.link(manager)
           manager.start
       }
    }
    
    /**
     * Function to parse and convert List[String] of passwd file entries to List[Account]
     * @author teamon
     */
    protected def allAccounts = {
        val rawData = Source.fromFile(Config.systemPasswdFile, "utf-8").getLines.toList
        for(Account(account) <- rawData) yield account
    }
     
     
    /**
     * Returns only normal users' accounts
     * @author teamon
     */
    protected def userAccounts = allAccounts.filter(_.isUser)
    
}