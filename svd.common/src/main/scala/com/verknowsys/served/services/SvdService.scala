package com.verknowsys.served.services


import com.verknowsys.served._
import com.verknowsys.served.api._
import com.verknowsys.served.api.scheduler._
import com.verknowsys.served.db._
import com.verknowsys.served.SvdConfig
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.Events._
import com.verknowsys.served.utils.signals.SvdPOSIX._
import com.verknowsys.served.services._
import com.verknowsys.served.api.accountkeys._
import com.verknowsys.served.api.pools._
import com.verknowsys.served.scheduler._

import net.liftweb.json._
import scala.io._
import scala.util._
import java.io.File
import akka.actor._
import akka.dispatch._
import akka.pattern.ask
import akka.remote._
import akka.util.Duration
import akka.util.Timeout
import akka.util.duration._
import java.lang.System

import org.quartz._
import org.quartz.impl._


/**
 *
 *  @author dmilith
 *  Service actor. All Svd services are started using this actor wrapper.
 *
 */
class SvdService(config: SvdServiceConfig, account: SvdAccount) extends SvdActor with SvdUtils {

    // import akka.actor.OneForOneStrategy
    // import akka.actor.SupervisorStrategy._

    // override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 25, withinTimeRange = 10 seconds) {
    //     // case _: Terminated               => Restart
    //     case _: ArithmeticException      => Restart
    //     case _: NullPointerException     => Restart
    //     case _: IllegalArgumentException => Restart
    //     case _: Exception                => Restart
    // }


    val uptime = System.currentTimeMillis // Service uptime measure point
    val serviceRootPrefix = SvdConfig.userHomeDir / "%s".format(account.uid) / SvdConfig.applicationsDir / config.name
    val servicePrefix = SvdConfig.userHomeDir / "%d".format(account.uid) / SvdConfig.softwareDataDir / config.name
    val accountManager = context.actorFor("/user/SvdAccountManager")
    val autostartFileLocation = servicePrefix / SvdConfig.serviceAutostartFile

    val installIndicator = new File(
        if (account.uid == 0)
            SvdConfig.systemHomeDir / "%s".format(account.uid) / SvdConfig.applicationsDir / config.name / config.name.toLowerCase + "." + SvdConfig.installed
        else
            serviceRootPrefix / config.name.toLowerCase + "." + SvdConfig.installed
    )
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


    def replaceAllSpecialValues(hook: String) =
        hook
        .replaceAll("SERVICE_PREFIX", servicePrefix)
        .replaceAll("SERVICE_ADDRESS", SvdConfig.defaultHost)
        .replaceAll("SERVICE_DOMAIN", SvdConfig.defaultDomain)
        .replaceAll("SERVICE_ROOT", serviceRootPrefix)
        .replaceAll("SERVICE_VERSION", try {
            Source.fromFile(installIndicator).mkString
        } catch {
            case _: Exception => "no-version"
        })
        .replaceAll("SERVICE_PORT", "%d".format(servicePort))


    /**
     *  @author dmilith
     *
     *   hookShot is a safe way to launch service hooks
     */
    def hookShot(hook: SvdShellOperations, hookName: String) { // XXX: this should be done better. String should be replaced
        if (!hook.commands.isEmpty) { // don't report empty / undefined hooks
            try {
                val execCommands = hook.commands.map {
                    replaceAllSpecialValues _
                }
                shell.exec(
                    hook.copy(commands = execCommands)
                )
                // INFO -- @deldagorin/192.168.0.3 -- Done start of service: Nginx
                val matcher = """after.*""".r
                hookName match {
                    case matcher() =>
                        log.trace("MATCHED: %s".format(hookName))
                        if (config.reportAllDebugs)
                            accountManager ! Notify.Message(formatMessage("D:Done %s of service: %s".format(hookName, config.name)))

                    case "validate" =>
                        if (config.reportAllDebugs)
                            accountManager ! Notify.Message(formatMessage("D:Done %s of service: %s".format(hookName, config.name)))

                    case _ =>
                        if (config.reportAllInfos)
                            accountManager ! Notify.Message(formatMessage("I:Done %s of service: %s".format(hookName, config.name)))
                }

            } catch {
                case x: expectj.TimeoutException =>
                    val hk = hook.copy( commands = hook.commands.map { // map values for better message
                        replaceAllSpecialValues _
                    })
                    val msg = formatMessage("E:Hook %s of service: %s failed to pass expectations: CMD: '%s', OUT: '%s', ERR: '%s'.".format(hookName, config.name, hk.commands.mkString(" "), hk.expectStdOut, hk.expectStdErr))
                    log.error(msg)
                    if (config.reportAllErrors)
                        accountManager ! Notify.Message(msg)

                case x: Exception =>
                    val msg = formatMessage("F:Thrown exception in hook: %s of service: %s an exception content below:\n%s".format(hookName, config.name, x.getMessage + " " + x.getStackTrace))
                    log.error(msg)
                    if (config.reportAllErrors)
                        accountManager ! Notify.Message(msg)
            }
        } else {
            log.trace("Command list empty in hook: %s of service %s".format(hookName, config.name))
        }
    }


    override def preStart = {

        /* check for previous installation */
        log.info("Looking for %s file to check software installation status".format(installIndicator))
        installIndicator.exists match {
            case true =>
                log.info("Service already installed: %s".format(config.name))

            case false =>
                log.info("Installing service: %s".format(config.name))
                hookShot(installHook, "install")
                hookShot(configureHook, "configure")
        }
        hookShot(validateHook, "validate")

        // val pause = SvdConfig.serviceRestartPause / 2
        // log.debug("Waiting after validate hook for: %ss".format(pause/1000))
        // Thread.sleep(pause)

        if (config.autoStart) {
            log.info("Starting service: %s", config.name)
            hookShot(startHook, "start")
            hookShot(afterStartHook, "afterStart")
        }

        // defining scheduler job
        if (!config.schedulerActions.isEmpty) {
            val amount = config.schedulerActions.length
            log.trace("Scheduler triggers defined: %d".format(amount))
            for (index <- 0 to amount - 1) {
                log.trace("Proceeding with index: %d", index)
                val action = config.schedulerActions(index)

                log.debug("Config scheduler actions for service %s aren't empty.".format(config.name))
                try {
                    val name = config.name
                    val jobInstance = new ShellJob
                    val job = JobBuilder.newJob(jobInstance.getClass)
                        .withIdentity("%s-%d".format(name, index))
                        .build

                    // setting job data values:
                    val cronEntry = CronScheduleBuilder.cronSchedule(action.cronEntry)
                    val shellOperations = SvdShellOperations(commands = action.shellCommands.map {
                            elem => replaceAllSpecialValues(elem)
                        })
                    job.getJobDataMap.put("shellOperations", shellOperations)
                    job.getJobDataMap.put("account", account)
                    val trigger = TriggerBuilder.newTrigger
                        .withIdentity("%s-%d".format(name, index))
                        .startNow
                        .withSchedule(cronEntry)
                        .build

                    accountManager ! SvdScheduler.StartJob(name, job, trigger)

                } catch {
                    case e: java.text.ParseException =>
                        accountManager ! Notify.Message(formatMessage("E:%s".format(e)))

                    case e: Throwable =>
                        accountManager ! Notify.Message(formatMessage("F:%s".format(e)))
                }

            }
        }

    }


    def receive = {

        case User.ServiceAutostart =>
            touch(autostartFileLocation)
            val msg = formatMessage("I:Turned on autostart of: %s".format(this))
            log.debug(msg)
            accountManager ! Notify.Message(msg)

        case User.ServiceStatus =>
            val msg = formatMessage("I:%s".format(this))
            log.debug(msg)
            accountManager ! Notify.Message(msg)

        case Ping =>
            log.debug("%s".format(this))
            sender ! Pong

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
            log.info("Got Quit in %s".format(this))
            hookShot(stopHook, "stop")
            hookShot(afterStopHook, "afterStop")
            context.stop(self)

        case Success =>
            log.trace("Success in SvdService from %s".format(sender.getClass.getName))
    }

    // addShutdownHook {
    //     log.warn("SvdService: %s shutdown hook invoked".format(config.name))
    //     // postStop
    // }

    override def postStop {
        log.info("PostStop in SvdService: %s".format(config.name))
        log.debug("Stopping scheduler for service.")
        accountManager ! SvdScheduler.StopJob(config.name)

        hookShot(stopHook, "stop")
        hookShot(afterStopHook, "afterStop")
        log.info("Stopped SvdService: %s".format(config.name))
        super.postStop
    }


    override def toString = "SvdService name: %s. Uptime: %s".format(config.name, secondsToHMS((System.currentTimeMillis - uptime).toInt / 1000))

}
