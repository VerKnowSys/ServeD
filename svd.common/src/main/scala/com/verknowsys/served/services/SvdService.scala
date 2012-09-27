package com.verknowsys.served.services


import com.verknowsys.served.db._
import com.verknowsys.served.SvdConfig
import com.verknowsys.served.utils._
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.api._

import java.io.File
import akka.actor.Actor


class SvdService(config: SvdServiceConfig, account: SvdAccount) extends SvdExceptionHandler {


    lazy val shell = new SvdShell(account)
    lazy val installIndicator = new File(
        if (account.uid == 0)
            SvdConfig.systemHomeDir / "%s".format(account.uid) / SvdConfig.applicationsDir / config.name / SvdConfig.installed
        else
            SvdConfig.userHomeDir / "%s".format(account.uid) / SvdConfig.applicationsDir / config.name / SvdConfig.installed
    )


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
    def installHook = config.install ::: SvdShellOperation("touch %s".format(installIndicator)) :: Nil


    /**
     *  @author dmilith
     *
     *   reloadHook - Service reloading command
     *   Will be executed only on demand, by sending Reload signal to SvdService
     */
    def reloadHook = config.reload


    /**
     *  @author dmilith
     *
     *   validateHook - Performed right after configure on each application run
     *   Will throw exception when validation process wont pass
     */
    def validateHook = config.validate


    log.debug("SvdService install started for: %s".format(config.name))

    /* check for previous installation */
    log.trace("Looking for %s file to check software installation status".format(installIndicator))
    if (!installIndicator.exists) {
        log.info("Performing installation of software: %s".format(config.name))
        installHook.foreach {
            hook =>
                log.trace("installHook: %s".format(hook))
                shell.exec(hook)
        }
        configureHook.foreach {
            hook =>
                log.trace("configureHook: %s".format(hook))
                shell.exec(hook)
        }
    } else {
        log.info("Software already installed: %s".format(config.name))
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
         *   Reload by default should be SIGHUP signal sent to process pid
         */
        case Reload =>
            validateHook.foreach {
                hook =>
                    log.trace("validateHook: %s".format(hook))
                    shell.exec(hook)
            }
            reloadHook.foreach {
                hook =>
                    log.trace("reloadHook: %s".format(hook))
                    shell.exec(hook)
            }
            sender ! Success

        /**
         *  @author dmilith
         *
         *   Run should be sent when we want to start this service.
         */
        case Run =>
            log.info("SvdService with name %s has been started".format(config.name))
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
            sender ! Success

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
            // shell.close
            sender ! Success

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
        log.info("Stopped SvdService: %s".format(config.name))
    }


    override def toString = "SvdService name: %s with config: %s".format(config.name, config)

}

// class SvdSystemService extends SvdExceptionHandler

// class SvdUserService extends SvdExceptionHandler