package com.verknowsys.served.systemmanager.managers

import com.verknowsys.served.SvdConfig
import com.verknowsys.served.api._
import com.verknowsys.served.db._
import com.verknowsys.served.utils._
import com.verknowsys.served.systemmanager.native._

import akka.actor.Actor.{remote, actorOf, registry}
import akka.actor.Actor
import com.verknowsys.served.systemmanager.SvdSystemManager
import com.verknowsys.served.systemmanager.SvdAccountsManager


object Accounts extends DB[SvdAccount]


/**
 * Account Manager - owner of all managers
 * 
 * @author teamon
 */
class SvdAccountManager(val uid: Int) extends Actor with SvdExceptionHandler {
    
    log.info("Starting AccountManager for uid: %s".format(uid))
    
    val server = new DBServer(9009, SvdConfig.userHomeDir / "%s".format(uid) / "%s.userdb".format(uid)) // 2011-06-26 00:20:59 - dmilith - XXX: hardcoded port name
    val db = server.openClient
    val account = Accounts(db)(_.uid == uid).headOption.getOrElse {
        val newAccount = new SvdAccount(uid = uid, gid = uid)
        db << newAccount
        newAccount
    }
    log.debug("Got user account: %s".format(account))
    
    val sh = new SvdShell(account)
    val gitManager = Actor.actorOf(new SvdGitManager(account, db))
    self startLink gitManager
    

    def receive = {
        case Init =>
            log.info("SvdAccountManager received Init.")
            // SvdSystemManager ! GetAllProcesses
            // SvdSystemManager ! GetNetstat
            
            sh.exec("rm -rf /Users/501/THE_DB_by_initdb && initdb -D /Users/501/THE_DB_by_initdb && pg_ctl -D /Users/501/THE_DB_by_initdb start && sleep 45 && pg_ctl -D /Users/501/THE_DB_by_initdb stop")
            log.debug("OUTPUT: " + sh.output(0).head)
            sh.close(0)
            sh.exec("ls -lam /usr")
            
            val psAll = SvdLowLevelSystemAccess.processList(true)
            log.debug("All user process IDs: %s".format(psAll.mkString(", ")))
            
        case msg: git.Base => 
            gitManager forward msg
    }
    
    
    override def postStop {
        sh.closeAll
        db.close
        server.close
    }
    
}
