package com.verknowsys.served.maintainer

import scala.actors.Actor
import scala.io.Source
import scala.collection.mutable.ListBuffer

import com.verknowsys.served.Config
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.utils.monitor.MonitoredActor
import com.verknowsys.served.managers.AccountManager
import com.verknowsys.served.kqueue.Kqueue

// case class GetUsers(val list: List[Account])



object AccountsManager extends MonitoredActor with Utils {
    case object ReloadUsers
    
    start

    val managers = new ListBuffer[AccountManager]()
        
    def act {
        // TODO: Catch java.io.FileNotFoundException and exit. ServeD can`t run withour passwd file
        val watchPasswdFile = Kqueue.watch(Config.systemPasswdFile, modified = true) {
            logger.trace("Triggered (modified/created) system password file: %s".format(Config.systemPasswdFile))
            this ! ReloadUsers
        }    
        
        logger.debug("Initialized watch for " + Config.systemPasswdFile)
        logger.trace("watchPasswordFile: " + watchPasswdFile)
        
        loop {
            receive {
                case Init =>
                    this ! ReloadUsers            
                    logger.info("AccountManager ready")
                    
                case Quit =>
                    logger.info("Quitting AccountManager")
                    // watchPasswdFile.stop
                    
                case ReloadUsers =>
                    logger.trace("Reloading users list")
                    // How does it work
                    //
                    // <managers> = ListBuffer[AccountManager]
                    // 1. Start
                    //  - loadAccounts from /etc/passwd
                    //  - for each Account spawn AccountManager and add it to <managers>
                    //  - setup FileWatcher on /etc/passwd
                    //
                    // 2. /etc/passwd changed
                    // - read Accounts list form /etc/passwd
                    // - for every Account 
                    //   - check if there is existing AccountManager
                    //     if not, spawn AccountManager and add it to <managers>
                    // - for every AccountManager from <managers>
                    //   - check if it`s Account is in new accounts list
                    //     - if not, send Quit to manager and remove it from list
                    //
                    // TODO: Update user`s shell path
                    
                    val accounts = userAccounts
                    
                    logger.trace("accounts: " + accounts)
                                        
                    accounts foreach { a =>
                        if(!managers.exists(_.account == a)) managers += new AccountManager(a)
                    }
                    
                    managers.foreach { m =>
                        if(!accounts.exists(_ == m.account)){
                            m ! Quit
                            managers -= m
                        }
                    }
                    
                    logger.debug(watchPasswdFile + " " + watchPasswdFile.getState)
                
                case x: AnyRef =>
                    logger.warn("Command not recognized. AccountManager will ignore You: " + x.toString)
                    
            }
        }
    }



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
    
    /**
     * Function to parse and convert List[String] of passwd file entries to List[Account]
     * @author teamon
     */
    protected def allAccounts = {
        val rawData = Source.fromFile(Config.systemPasswdFile, "utf-8").getLines.toList
        for(Account(account) <-rawData) yield account
    }

    
    /**
     * Returns only normal users' accounts
     * @author teamon
     */
    protected def userAccounts = allAccounts.filter(_.isUser)
    
    /**
     * @author teamon
     */
    override def toString = "!AccountsManager!"

}
