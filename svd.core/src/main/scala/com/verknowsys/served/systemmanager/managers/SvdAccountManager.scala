package com.verknowsys.served.systemmanager.managers

import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.utils.SvdExceptionHandler
import com.verknowsys.served.api._
import com.verknowsys.served.utils.Logging
import com.verknowsys.served.db._
import com.verknowsys.served.utils._

import akka.actor.Actor.{remote, actorOf, registry}
import akka.actor.Actor
import com.verknowsys.served.systemmanager.SvdSystemManager
import expectj._


case object GetAccount


/**
 * Account Manager - owner of all managers
 * 
 * @author teamon
 */
class SvdAccountManager(val account: SvdAccount) extends Actor with SvdExceptionHandler {
    log.info("Starting AccountManager for account: " + account)
    
    val dbServer = new DBServer(9000, account.homeDir / "config")
    
    val gitManager = Actor.actorOf(new SvdGitManager(account, dbServer.openClient))
    self startLink gitManager
    
    // val gatherer = Actor.actorOf(new SvdGatherer(account))
    // self startLink gatherer


    def receive = {
        case Init =>
            log.info("SvdAccountManager received Init.")
            SvdSystemManager ! GetAllProcesses
            SvdSystemManager ! GetNetstat
            
            // Create a new ExpectJ object with a timeout of 5s
            val expectinator = new ExpectJ(5) 

            // Fork the process
            val shell = expectinator.spawn("/bin/sh")
            shell.send("export USER=%s\n".format(account.userName))
            shell.send("export USERNAME=%s\n".format(account.userName))
            shell.send("export EDITOR=true\n")
            shell.send("cd %s\n".format(account.homeDir))
            
            shell.send("env\n")
            shell.send("pwd\n")
            
            // shell.send("set -v off")
            shell.send("echo Chunder\n")
            shell.expect("Chunder")
            shell.send("echo $USER\n")
            // shell.expect("dmilith")
            shell.send("rm -rf /Users/501/THE_DB_by_initdb\n")
            shell.send("initdb -D /Users/501/THE_DB_by_initdb\n")
            shell.expect("Success. You can now start the database server using:")

            log.warn(shell.getCurrentStandardOutContents)
            log.warn(shell.getCurrentStandardErrContents)

            shell.send("exit\n")
            shell.expectClose
        
        
        case GetAccount => 
            self reply account
            
        case msg: git.Base => 
            gitManager forward msg
    }
    
    override def postStop {
        dbServer.close
    }
    
}
