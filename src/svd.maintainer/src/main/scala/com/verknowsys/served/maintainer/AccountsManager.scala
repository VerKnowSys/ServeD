package com.verknowsys.served.maintainer

import scala.actors.Actor
import scala.io.Source
import scala.collection.mutable.ListBuffer

import com.verknowsys.served.Config
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.utils.monitor.MonitoredActor
import com.verknowsys.served.managers.AccountManager

// case class GetUsers(val list: List[Account])




object AccountsManager extends MonitoredActor with Utils {
    case object ReloadUsers
    
    start

    val managers = new ListBuffer[AccountManager]()
        
    def act {
        logger.trace("Java Library Path Property: " + System.getProperty("java.library.path"))
        
        val watchPasswordFile = FileEvents.watchFile(Config.passwdFileName) {
            logger.trace("Triggered (modified/created) system password file: %s".format(Config.passwdFileName))
        }    
        logger.debug("Initialized watch for " + Config.passwdFileName)
        logger.trace("watchPasswordFile: " + watchPasswordFile)
        
        loop {
            receive {
                case Init =>
                    this ! ReloadUsers            
                    logger.info("AccountManager ready for tasks")
                    
                case Quit =>
                    logger.info("Quitting AccountManager…")
                    watchPasswordFile.stop
                    
                case ReloadUsers =>
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
                    
                    accounts.foreach { a =>
                        if(!managers.exists(_.account.equals(a))){
                            managers += new AccountManager(a)
                        }
                    }
                    
                    managers.foreach { m =>
                        if(!accounts.exists(_.equals(m.account))){
                            m ! Quit
                            managers -= m
                        }
                    }
                
                case x: AnyRef =>
                    logger.warn("Command not recognized. AccountManager will ignore You: " + x.toString)
                    
            }
        }
    }



    /**
     * Function to parse and convert List[String] of passwd file entries to List[Account]
     * @author dmilith
     */
    protected def allAccounts = {
        val rawData = Source.fromFile(Config.systemPasswdFile, "utf-8").getLines.toList
        for(line <- rawData if !line.startsWith("#")) // XXX: hardcode
            yield
                new Account(line.split(":").toList)
    }
    
    /**
     * Returns only normal users' accounts
     * @author teamon
     */
    protected def userAccounts = allAccounts.filter(_.isUser)

}
