package com.verknowsys.served.services


import com.verknowsys.served._
import com.verknowsys.served.db._
import com.verknowsys.served.SvdConfig
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.events._
import com.verknowsys.served.utils.signals.SvdPOSIX._
import com.verknowsys.served.services._
import com.verknowsys.served.api._
import com.verknowsys.served.api.accountkeys._
import com.verknowsys.served.api.pools._

import net.liftweb.json._
import scala.io._
import java.io.File
import akka.actor._
import akka.dispatch._
import akka.pattern.ask
import akka.remote._
import akka.util.Duration
import akka.util.Timeout
import akka.util.duration._


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


class SvdService(config: SvdServiceConfig, account: SvdAccount, notificationsManager: ActorRef, accountManager: ActorRef) extends SvdExceptionHandler with SvdUtils {

    val installIndicator = new File(
        if (account.uid == 0)
            SvdConfig.systemHomeDir / "%s".format(account.uid) / SvdConfig.applicationsDir / config.name / config.name.toLowerCase + "." + SvdConfig.installed
        else
            SvdConfig.userHomeDir / "%s".format(account.uid) / SvdConfig.applicationsDir / config.name / config.name.toLowerCase + "." + SvdConfig.installed
    )
    val servicePrefix = SvdConfig.userHomeDir / "%d".format(account.uid) / SvdConfig.softwareDataDir / config.name
    checkOrCreateDir(servicePrefix)

    implicit val timeout = Timeout(60 seconds) // XXX: hardcode
    val future = accountManager ? Admin.GetPort
    val servicePort = Await.result(future, timeout.duration).asInstanceOf[Int] // get any random port
    log.trace("Expected port from Account Manager arrived: %d".format(servicePort))

    def shell = new SvdShell(account)


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
    def hookShot(hook: SvdShellOperations, hookName: String) { // XXX: this should be done better. String should be replaced
        if (!hook.commands.isEmpty) { // don't report empty / undefined hooks
            try {
                val execCommands = hook.commands.map {
                    _.replaceAll("SERVICE_PREFIX", servicePrefix)
                     .replaceAll("SERVICE_ROOT", SvdConfig.userHomeDir / "%d".format(account.uid) / SvdConfig.applicationsDir / config.name)
                     .replaceAll("SERVICE_VERSION", try {
                            Source.fromFile(installIndicator).mkString
                        } catch {
                            case _: Exception => "no-version"
                        })
                    .replaceAll("SERVICE_PORT", "%d".format(servicePort))
                }
                shell.exec(
                    hook.copy(commands = execCommands)
                )
                val msg = "--- INFO ---\nPerforming %s of service: %s\n------------\n".format(hookName, config.name)
                log.trace(msg)
                if (config.reportAllInfos)
                    notificationsManager ! Notify.Message(msg)

            } catch {
                case x: expectj.TimeoutException =>
                    val hk = hook.copy( commands = hook.commands.map { // map values for better message
                        _.replaceAll("SERVICE_PREFIX", servicePrefix)
                         .replaceAll("SERVICE_PORT", "*(masked-random-user-port)*")
                         .replaceAll("SERVICE_ROOT", SvdConfig.userHomeDir / "%d".format(account.uid) / SvdConfig.applicationsDir / config.name)
                    })
                    val msg = "=== ERROR ===\nHook %s of service: %s failed to pass expectations: CMD: '%s', OUT: '%s', ERR: '%s'.\n=============\n".format(hookName, config.name, hk.commands.mkString(" "), hk.expectStdOut, hk.expectStdErr)
                    log.error(msg)
                    if (config.reportAllErrors)
                        notificationsManager ! Notify.Message(msg)

                case x: Exception =>
                    val msg = "### FATAL ###\nThrown exception in hook: %s of service: %s an exception content below:\n%s\n#############\n".format(hookName, config.name, x.getMessage + " " + x.getStackTrace)
                    log.error(msg)
                    if (config.reportAllErrors)
                        notificationsManager ! Notify.Message(msg)
            }
        } else {
            log.trace("Command list empty.")
        }
    }


    override def preStart = {
        // log.debug("SvdService install started for: %s".format(config.name))

        /* check for previous installation */
        log.info("Looking for %s file to check software installation status".format(installIndicator))
        if (!installIndicator.exists) {
            hookShot(installHook, "install")
            hookShot(configureHook, "configure")
        } else {
            log.info("Service software already installed and configured: %s".format(config.name))
        }
        hookShot(validateHook, "validate")
        if (config.autoStart) {
            log.info("Starting service: %s", config.name)
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
            sender ! Success

        /**
         *  @author dmilith
         *
         *   Quit should be sent when we want to stop this service
         */
        case Quit =>
            hookShot(stopHook, "stop")
            hookShot(afterStopHook, "afterStop")
            sender ! Success

        case Success =>
            log.trace("Success in SvdService from %s".format(sender.getClass.getName))
    }


    override def postStop {
        log.info("PostStop in SvdService: %s".format(config.name))
        hookShot(stopHook, "stop")
        hookShot(afterStopHook, "afterStop")
        log.info("Stopped SvdService: %s".format(config.name))
        super.postStop
    }


    override def toString = "SvdService name: %s with config: %s".format(config.name, config)

}

// class SvdSystemService extends SvdExceptionHandler

// class SvdUserService extends SvdExceptionHandler