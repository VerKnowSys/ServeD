package com.verknowsys.served.services


import com.verknowsys.served.db._
import com.verknowsys.served.SvdConfig
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.api._
import com.verknowsys.served.utils._

import net.liftweb.json._
import scala.io._
import java.io.File
import akka.actor._


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
    // log.trace("Default template: %s".format(defaultTemplate))
    // log.trace("App template: %s".format(appSpecificTemplate))
    log.trace("Merged template: %s".format(compact(render(appTemplateMerged))))

    val config = SvdServiceConfig( // OPTIMIZE: this should be done automatically
        name = (appTemplateMerged \ "name").extract[String],
        autoRestart = (appTemplateMerged \ "autoRestart").extract[Boolean],
        autoStart = (appTemplateMerged \ "autoStart").extract[Boolean],
        reportAllErrors = (appTemplateMerged \ "reportAllErrors").extract[Boolean],
        reportAllInfos = (appTemplateMerged \ "reportAllInfos").extract[Boolean],
        install = SvdShellOperations(
                commands = (appTemplateMerged \ "install" \ "commands").extract[List[String]],
                expectStdOut = (appTemplateMerged \ "install" \ "expectStdOut").extract[List[String]],
                expectStdErr = (appTemplateMerged \ "install" \ "expectStdErr").extract[List[String]],
                expectOutputTimeout = (appTemplateMerged \ "install" \ "expectOutputTimeout").extract[Int]
            ),
        configure = SvdShellOperations(
                commands = (appTemplateMerged \ "configure" \ "commands").extract[List[String]],
                expectStdOut = (appTemplateMerged \ "configure" \ "expectStdOut").extract[List[String]],
                expectStdErr = (appTemplateMerged \ "configure" \ "expectStdErr").extract[List[String]],
                expectOutputTimeout = (appTemplateMerged \ "configure" \ "expectOutputTimeout").extract[Int]
            ),
        start = SvdShellOperations(
                commands = (appTemplateMerged \ "start" \ "commands").extract[List[String]],
                expectStdOut = (appTemplateMerged \ "start" \ "expectStdOut").extract[List[String]],
                expectStdErr = (appTemplateMerged \ "start" \ "expectStdErr").extract[List[String]],
                expectOutputTimeout = (appTemplateMerged \ "start" \ "expectOutputTimeout").extract[Int]
            ),
        afterStart = SvdShellOperations(
                commands = (appTemplateMerged \ "afterStart" \ "commands").extract[List[String]],
                expectStdOut = (appTemplateMerged \ "afterStart" \ "expectStdOut").extract[List[String]],
                expectStdErr = (appTemplateMerged \ "afterStart" \ "expectStdErr").extract[List[String]],
                expectOutputTimeout = (appTemplateMerged \ "afterStart" \ "expectOutputTimeout").extract[Int]
            ),
        stop = SvdShellOperations(
                commands = (appTemplateMerged \ "stop" \ "commands").extract[List[String]],
                expectStdOut = (appTemplateMerged \ "stop" \ "expectStdOut").extract[List[String]],
                expectStdErr = (appTemplateMerged \ "stop" \ "expectStdErr").extract[List[String]],
                expectOutputTimeout = (appTemplateMerged \ "stop" \ "expectOutputTimeout").extract[Int]
            ),
        afterStop = SvdShellOperations(
                commands = (appTemplateMerged \ "afterStop" \ "commands").extract[List[String]],
                expectStdOut = (appTemplateMerged \ "afterStop" \ "expectStdOut").extract[List[String]],
                expectStdErr = (appTemplateMerged \ "afterStop" \ "expectStdErr").extract[List[String]],
                expectOutputTimeout = (appTemplateMerged \ "afterStop" \ "expectOutputTimeout").extract[Int]
            ),
        reload = SvdShellOperations(
                commands = (appTemplateMerged \ "reload" \ "commands").extract[List[String]],
                expectStdOut = (appTemplateMerged \ "reload" \ "expectStdOut").extract[List[String]],
                expectStdErr = (appTemplateMerged \ "reload" \ "expectStdErr").extract[List[String]],
                expectOutputTimeout = (appTemplateMerged \ "reload" \ "expectOutputTimeout").extract[Int]
            ),
        validate = SvdShellOperations(
                commands = (appTemplateMerged \ "validate" \ "commands").extract[List[String]],
                expectStdOut = (appTemplateMerged \ "validate" \ "expectStdOut").extract[List[String]],
                expectStdErr = (appTemplateMerged \ "validate" \ "expectStdErr").extract[List[String]],
                expectOutputTimeout = (appTemplateMerged \ "validate" \ "expectOutputTimeout").extract[Int]
            )
        )

}


class SvdService(config: SvdServiceConfig, account: SvdAccount, notificationsManager: ActorRef) extends SvdExceptionHandler {

    lazy val shell = new SvdShell(account)
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


    /**
     *  @author dmilith
     *
     *   hookShot is a safe way to launch service hooks
     */
    def hookShot(hook: SvdShellOperations, hookName: String) {
        // catch exceptions from expectinator:
        try {
            shell.exec(hook)
            val msg = "--- INFO ---\nPerforming %s of service: %s\n------------\n".format(hookName, config.name)
            log.trace(msg)
            if (config.reportAllInfos)
                notificationsManager ! Notify.Message(msg)
        } catch {
            case x: expectj.TimeoutException =>
                val msg = "=== ERROR ===\nHook %s of service: %s failed to pass expectations: CMD: '%s', OUT: '%s', ERR: '%s'.\n=============\n".format(hookName, config.name, hook.commands.mkString(" "), hook.expectStdOut, hook.expectStdErr)
                log.error(msg)
                if (config.reportAllErrors)
                    notificationsManager ! Notify.Message(msg)

            case x: Exception =>
                val msg = "### FATAL ###\nThrown exception in hook: %s of service: %s an exception content below:\n%s\n#############\n".format(hookName, config.name, x)
                log.error(msg)
                if (config.reportAllErrors)
                    notificationsManager ! Notify.Message(msg)
        }
    }


    override def preStart = {
        log.debug("SvdService install started for: %s".format(config.name))

        /* check for previous installation */
        log.trace("Looking for %s file to check software installation status".format(installIndicator))
        if (!installIndicator.exists) {
            hookShot(installHook, "install")
            hookShot(configureHook, "configure")
        } else {
            log.info("Service software already installed and configured: %s".format(config.name))
        }
        hookShot(validateHook, "validate")
        if (config.autoStart) {
            hookShot(startHook, "start")
            hookShot(afterStartHook, "afterStart")
        }
    }


    def receive = {

        /**
         *  @author dmilith
         *
         *   Reload by default should be SIGHUP signal sent to process pid
         */
        case Reload =>
            hookShot(validateHook, "validate")
            hookShot(reloadHook, "reload")

        /**
         *  @author dmilith
         *
         *   Explicit method to launch service
         */
        case Run =>
            hookShot(startHook, "start")
            hookShot(afterStartHook, "afterStart")

        /**
         *  @author dmilith
         *
         *   Quit should be sent when we want to stop this service
         */
        case Quit =>
            hookShot(stopHook, "stop")
            hookShot(afterStopHook, "afterStop")

        case Success =>
            log.trace("Success in SvdService from %s".format(sender.getClass.getName))
    }


    override def postStop {
        hookShot(stopHook, "stop")
        hookShot(afterStopHook, "afterStop")
        // shell.close
        log.info("Stopped SvdService: %s".format(config.name))
        super.postStop
    }


    override def toString = "SvdService name: %s with config: %s".format(config.name, config)

}

// class SvdSystemService extends SvdExceptionHandler

// class SvdUserService extends SvdExceptionHandler