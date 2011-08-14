package com.verknowsys.served.services


import com.verknowsys.served.db._
import com.verknowsys.served.SvdConfig
import com.verknowsys.served.utils._
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.api._

import akka.actor.Actor


class SvdService(account: SvdAccount, name: String) extends SvdExceptionHandler {    
    
    /**
     *  @author dmilith
     *
     *   configureHook - will be executed before starting service actor
     */
    def configureHook: List[SvdShellOperation] = Nil


    /**
     *  @author dmilith
     *
     *   afterStartHook - will be executed after starting of service
     */
    def afterStartHook: List[SvdShellOperation] = Nil


    /**
     *  @author dmilith
     *
     *   startHook - hook executed on service start
     */
    def startHook: List[SvdShellOperation] = Nil


    /**
     *  @author dmilith
     *
     *   stopHook - stop hook executed on stop
     */
    def stopHook: List[SvdShellOperation] = Nil


    /**
     *  @author dmilith
     *
     *   afterStopHook - will be executed after service stop
     */
    def afterStopHook: List[SvdShellOperation] = Nil
    

    lazy val shell = new SvdShell(account)
    configureHook.foreach {
        hook =>
            log.trace("configureHook: %s".format(hook))
            shell.exec(hook)
    }
    
    
    def receive = {
        
        /**
         *  @author dmilith
         *
         *   Run should be sent when we want to start this service
         */
        case Run =>
            log.debug("SvdService with name %s has been started".format(name))
            startHook.foreach {
                hook =>
                    log.trace("startHook: %s".format(hook))
                    shell.exec(hook)
            }
            afterStartHook.foreach {
                hook =>
                    log.trace("afterStartHook: %s".format(hook))
                    shell.exec(hook)
            }
            self reply Success

        /**
         *  @author dmilith
         *
         *   Quit should be sent when we want to stop this service
         */
        case Quit =>
            stopHook.foreach {
                hook =>
                    log.trace("stopHook: %s".format(hook))
                    shell.exec(hook)
            }
            afterStopHook.foreach {
                hook =>
                    log.trace("afterStopHook: %s".format(hook))
                    shell.exec(hook)
            }
            shell.close
            self reply Success
            
    }
    
}

// class SvdSystemService extends SvdExceptionHandler

// class SvdUserService extends SvdExceptionHandler