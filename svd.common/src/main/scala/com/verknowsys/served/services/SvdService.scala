package com.verknowsys.served.services


import com.verknowsys.served.db._
import com.verknowsys.served.SvdConfig
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.api._
import com.verknowsys.served.utils._

import net.liftweb.json._
import scala.io._
import java.io.File
import akka.actor.Actor


/**
 *
 * class to automatically load default configs of given software
 * it uses package object with implicit conversion from string to service configuration
 */
class SvdServiceConfigLoader(name: String) extends Logging {

    import net.liftweb.json._

    implicit val formats = DefaultFormats // Brings in default date formats etc.

    log.trace("SvdServiceConfigLoader: %s".format(name))

    val fullName = SvdConfig.defaultSoftwareTemplatesDir / name + SvdConfig.defaultSoftwareTemplateExt

    val defaultTemplate = parse(Source.fromFile(SvdConfig.defaultSoftwareTemplate + SvdConfig.defaultSoftwareTemplateExt).mkString) //.extract[SvdServiceConfig]
    val appSpecificTemplate = parse(Source.fromFile(fullName).mkString)
    val appTemplateMerged = defaultTemplate merge appSpecificTemplate

    // val svcName = (appSpecificTemplate \\ "name").extract[String]
    log.debug("Extracted SvdServiceConfig from igniter: %s.".format(name))
    log.trace("Default template: %s".format(defaultTemplate))
    log.trace("App template: %s".format(appSpecificTemplate))
    log.trace("Merged template: %s".format(appTemplateMerged))

    val config = SvdServiceConfig( // OPTIMIZE: this should be done automatically
        name = (appTemplateMerged \ "name").extract[String],
        install = SvdShellOperations(
                commands = (appTemplateMerged \ "install" \ "commands").extract[List[String]],
                expectStdOut = (appTemplateMerged \ "install" \ "expectStdOut").extract[List[String]],
                expectStdErr = (appTemplateMerged \ "install" \ "expectStdErr").extract[List[String]],
                waitForOutputFor = (appTemplateMerged \ "install" \ "waitForOutputFor").extract[Int]
            ),
        configure = SvdShellOperations(
                commands = (appTemplateMerged \ "configure" \ "commands").extract[List[String]],
                expectStdOut = (appTemplateMerged \ "configure" \ "expectStdOut").extract[List[String]],
                expectStdErr = (appTemplateMerged \ "configure" \ "expectStdErr").extract[List[String]],
                waitForOutputFor = (appTemplateMerged \ "configure" \ "waitForOutputFor").extract[Int]
            ),
        start = SvdShellOperations(
                commands = (appTemplateMerged \ "start" \ "commands").extract[List[String]],
                expectStdOut = (appTemplateMerged \ "start" \ "expectStdOut").extract[List[String]],
                expectStdErr = (appTemplateMerged \ "start" \ "expectStdErr").extract[List[String]],
                waitForOutputFor = (appTemplateMerged \ "start" \ "waitForOutputFor").extract[Int]
            ),
        afterStart = SvdShellOperations(
                commands = (appTemplateMerged \ "afterStart" \ "commands").extract[List[String]],
                expectStdOut = (appTemplateMerged \ "afterStart" \ "expectStdOut").extract[List[String]],
                expectStdErr = (appTemplateMerged \ "afterStart" \ "expectStdErr").extract[List[String]],
                waitForOutputFor = (appTemplateMerged \ "afterStart" \ "waitForOutputFor").extract[Int]
            ),
        stop = SvdShellOperations(
                commands = (appTemplateMerged \ "stop" \ "commands").extract[List[String]],
                expectStdOut = (appTemplateMerged \ "stop" \ "expectStdOut").extract[List[String]],
                expectStdErr = (appTemplateMerged \ "stop" \ "expectStdErr").extract[List[String]],
                waitForOutputFor = (appTemplateMerged \ "stop" \ "waitForOutputFor").extract[Int]
            ),
        afterStop = SvdShellOperations(
                commands = (appTemplateMerged \ "afterStop" \ "commands").extract[List[String]],
                expectStdOut = (appTemplateMerged \ "afterStop" \ "expectStdOut").extract[List[String]],
                expectStdErr = (appTemplateMerged \ "afterStop" \ "expectStdErr").extract[List[String]],
                waitForOutputFor = (appTemplateMerged \ "afterStop" \ "waitForOutputFor").extract[Int]
            ),
        reload = SvdShellOperations(
                commands = (appTemplateMerged \ "reload" \ "commands").extract[List[String]],
                expectStdOut = (appTemplateMerged \ "reload" \ "expectStdOut").extract[List[String]],
                expectStdErr = (appTemplateMerged \ "reload" \ "expectStdErr").extract[List[String]],
                waitForOutputFor = (appTemplateMerged \ "reload" \ "waitForOutputFor").extract[Int]
            ),
        validate = SvdShellOperations(
                commands = (appTemplateMerged \ "validate" \ "commands").extract[List[String]],
                expectStdOut = (appTemplateMerged \ "validate" \ "expectStdOut").extract[List[String]],
                expectStdErr = (appTemplateMerged \ "validate" \ "expectStdErr").extract[List[String]],
                waitForOutputFor = (appTemplateMerged \ "validate" \ "waitForOutputFor").extract[Int]
            )
        )

}


class SvdService(config: SvdServiceConfig, account: SvdAccount) extends SvdExceptionHandler {


    val shell = new SvdShell(account)
    lazy val installIndicator = new File(
        if (account.uid == 0)
            SvdConfig.systemHomeDir / "%s".format(account.uid) / SvdConfig.applicationsDir / config.name / config.name.toLowerCase + "." + SvdConfig.installed
        else
            SvdConfig.userHomeDir / "%s".format(account.uid) / SvdConfig.applicationsDir / config.name / config.name.toLowerCase + "." + SvdConfig.installed
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
    def installHook = config.install


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


    override def preStart = {
        log.debug("SvdService install started for: %s".format(config.name))

        /* check for previous installation */
        log.trace("Looking for %s file to check software installation status".format(installIndicator))
        if (!installIndicator.exists) {
            log.info("Performing installation of software: %s".format(config.name))
            log.trace("Installing service: %s".format(installHook))
            shell.exec(installHook)
            log.trace("Configuring service: %s".format(configureHook))
            shell.exec(configureHook)
        } else {
            log.info("Service software already installed and configured: %s".format(config.name))
        }

        log.trace("Performing validation of service for software: %s".format(validateHook))
        shell.exec(validateHook)

    }



    def receive = {

        /**
         *  @author dmilith
         *
         *   Reload by default should be SIGHUP signal sent to process pid
         */
        case Reload =>
            log.trace("Validating service configuration: %s".format(validateHook))
            shell.exec(validateHook)
            log.trace("Reloading service: %s".format(reloadHook))
            shell.exec(reloadHook)
            sender ! Success

        /**
         *  @author dmilith
         *
         *   Run should be sent when we want to start this service.
         */
        case Run =>
            log.info("SvdService with name %s has been started".format(config.name))
            log.trace("startHook: %s".format(startHook))
            shell.exec(startHook)
            log.trace("afterStartHook: %s".format(afterStartHook))
            shell.exec(afterStartHook)
            sender ! Success

        /**
         *  @author dmilith
         *
         *   Quit should be sent when we want to stop this service
         */
        case Quit =>
            log.trace("Executing stopHook command: %s", stopHook)
            shell.exec(stopHook)
            log.trace("Executing afterStopHook command: %s", afterStopHook)
            shell.exec(afterStopHook)

            // shell.close
            sender ! Success

    }


    override def postStop {
        log.trace("stopHook: %s".format(stopHook))
        shell.exec(stopHook)
        log.trace("afterStopHook: %s".format(afterStopHook))
        shell.exec(afterStopHook)
        shell.close
        log.info("Stopped SvdService: %s".format(config.name))
        super.postStop
    }


    override def toString = "SvdService name: %s with config: %s".format(config.name, config)

}

// class SvdSystemService extends SvdExceptionHandler

// class SvdUserService extends SvdExceptionHandler