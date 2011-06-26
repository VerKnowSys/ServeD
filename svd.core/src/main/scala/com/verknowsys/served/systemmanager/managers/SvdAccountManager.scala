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


/**
 * Account Manager - owner of all managers
 * 
 * @author teamon
 */
class SvdAccountManager(val account: SvdAccount) extends Actor with SvdExceptionHandler {
    
    log.info("Starting AccountManager for uid: %s".format(account.uid))
    
    val server = new DBServer(account.dbPort, SvdConfig.userHomeDir / "%s".format(account.uid) / "%s.db".format(account.uid))
    val db = server.openClient
    
    val sh = new SvdShell(account)
    val gitManager = Actor.actorOf(new SvdGitManager(account, db))
    self startLink gitManager
    

    def receive = {
        case Init =>
            log.info("SvdAccountManager received Init.")
            
            // sh.exec("rm -rf /Users/501/THE_DB_by_initdb && initdb -D /Users/501/THE_DB_by_initdb && pg_ctl -D /Users/501/THE_DB_by_initdb start && sleep 45 && pg_ctl -D /Users/501/THE_DB_by_initdb stop")
            //             log.debug("OUTPUT: " + sh.output(0).head)
            //             sh.close(0)
            //             sh.exec("ls -lam /usr")
            
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
