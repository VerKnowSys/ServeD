package com.verknowsys.served.systemmanager


import com.verknowsys.served.db._
import com.verknowsys.served.SvdConfig
import com.verknowsys.served.utils._
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.api._

import akka.actor.Actor


class SvdService(account: SvdAccount, name: String, actions: Seq[SvdShellOperation]) extends SvdExceptionHandler {
    
    val shell = new SvdShell(account)
    
    
    def receive = {
        
        case Init =>
            log.debug("SvdService with name %s has been started with actions: %s".format(name, actions.mkString(", ")))
            for (action <- actions)
                shell.exec(action)
            self reply Success

        case Quit =>
            shell.close
            self reply Success
            
    }
    
}

// class SvdSystemService extends SvdExceptionHandler

// class SvdUserService extends SvdExceptionHandler