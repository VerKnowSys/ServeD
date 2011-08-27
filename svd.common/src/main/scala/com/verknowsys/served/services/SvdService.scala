package com.verknowsys.served.services


import com.verknowsys.served.db._
import com.verknowsys.served.SvdConfig
import com.verknowsys.served.utils._
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.api._

import akka.actor.Actor


class SvdService(config: SvdServiceConfig, account: SvdAccount) extends SvdExceptionHandler {


    /**
     *  @author dmilith
     *
     *   configureHook - will be executed before starting service actor
     */
    def configureHook = config.configure


    /**
     *  @author dmilith
     *
     *   afterStartHook - will be executed after starting of service
     */
    def afterStartHook = config.afterStart


    /**
     *  @author dmilith
     *
     *   startHook - hook executed on service start
     */
    def startHook = config.start


    /**
     *  @author dmilith
     *
     *   stopHook - stop hook executed on stop
     */
    def stopHook = config.stop


    /**
     *  @author dmilith
     *
     *   afterStopHook - will be executed after service stop
     */
    def afterStopHook = config.afterStop


    /**
     *  @author dmilith
     *
     *   installHook - Software prepare / install hook.
     *   Will be executed only on demand, by sending Install signal to SvdService
     */
    def installHook = config.install


    /**
     *  @author dmilith
     *
     *   validateHook - Performed right after configure on each application run
     *   Will throw exception when validation process wont pass
     */
    def validateHook = config.validate


    lazy val shell = new SvdShell(account)
    configureHook.foreach {
        hook =>
            log.trace("configureHook: %s".format(hook))
            shell.exec(hook)
    }
    validateHook.foreach {
        hook =>
            log.trace("validateHook: %s".format(hook))
            shell.exec(hook)
    }


    def receive = {

        /**
         *  @author dmilith
         *
         *   Install should be sent to install required software for service.
         */
        case Install =>
            log.debug("SvdService install started for: %s".format(config.name))
            installHook.foreach {
                hook =>
                    log.trace("installHook: %s".format(hook))
                    shell.exec(hook)
            }

        /**
         *  @author dmilith
         *
         *   Run should be sent when we want to start this service
         */
        case Run =>
            log.debug("SvdService with name %s has been started".format(config.name))
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


    override def postStop {
        super.postStop
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
        log.info("Stopping SvdService: %s".format(config))
    }


}

// class SvdSystemService extends SvdExceptionHandler

// class SvdUserService extends SvdExceptionHandler