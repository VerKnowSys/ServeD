/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.services


import com.verknowsys.served.api._
import com.verknowsys.served.api.scheduler._
//import com.verknowsys.served.db._
import com.verknowsys.served.SvdConfig
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.utils._
import com.verknowsys.served.scheduler._

import scala.io._
import java.io.File
import java.text._
import akka.actor._
import scala.concurrent._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import java.lang.{System => JSystem}
import org.quartz.{TriggerBuilder, JobBuilder, CronScheduleBuilder}


/**
 *  Service Actor. All Svd services are started using this actor wrapper.
 *
 *  @author dmilith
 */
class SvdService(config: SvdServiceConfig, account: SvdAccount = SvdAccount(uid = 0)) extends SvdActor with SvdUtils with Logging {


    implicit val timeout = Timeout(SvdConfig.defaultAPITimeout / 1000 seconds)
    val className = this.getClass.getSimpleName

    val serviceRootPrefix = SvdConfig.userHomeDir / s"${account.uid}" / SvdConfig.applicationsDir / config.softwareName

    val servicePrefix = SvdConfig.userHomeDir / s"${account.uid}" / SvdConfig.softwareDataDir / config.name

    val accountManager = if (account.uid != 0) context.actorFor("/user/SvdAccountManager") else context.actorFor("/user/SvdAccountsManager")
    val uptime = JSystem.currentTimeMillis // Service uptime measure point
    lazy val autostartFileLocation = servicePrefix / SvdConfig.serviceAutostartFile
    lazy val future = accountManager ? System.GetPort
    lazy val sPort = Await.result(future, timeout.duration).asInstanceOf[Int] // get any random port

    lazy val portsFile = servicePrefix / ".service_ports"
    lazy val servicePort = if (config.staticPort == -1) {
        try {
            checkOrCreateDir(servicePrefix)
            Source.fromFile(portsFile).mkString.toInt
        } catch {
            case x: Exception =>
                checkOrCreateDir(servicePrefix)
                touch(portsFile)
                writeToFile(portsFile, s"${sPort}")
                sPort
        }
    } else {
        checkOrCreateDir(servicePrefix)
        log.debug(s"Static port given. Using ${config.staticPort}")
        writeToFile(portsFile, s"${config.staticPort}")
        config.staticPort
    }


    lazy val shell = new SvdShell(account)


    /**
     *  Returns file of install indication from Sofin.
     *
     *  @example "redis.installed" implies installed Redis software.
     *  @author dmilith
     */
    def installIndicator = new File(
        SvdConfig.userHomeDir / s"${account.uid}" / SvdConfig.applicationsDir / config.softwareName / config.softwareName.toLowerCase + "." + SvdConfig.installed)


    /**
     *   configureHook - will be executed before starting service actor
     *
     *  @author dmilith
     */
    def configureHook = config.configure


    /**
     *   afterStartHook - will be executed after starting of service
     *
     *  @author dmilith
     */
    def afterStartHook = config.afterStart


    /**
     *   startHook - hook executed on service start
     *
     *  @author dmilith
     */
    def startHook = config.start


    /**
     *   stopHook - stop hook executed on stop
     *
     *  @author dmilith
     */
    def stopHook = config.stop


    /**
     *   afterStopHook - will be executed after service stop
     *
     *  @author dmilith
     */
    def afterStopHook = config.afterStop


    /**
     *   installHook - Software prepare / install hook.
     *   Will be executed only on demand, by sending Install signal to SvdService
     *
     *  @author dmilith
     */
    def installHook = config.install


    /**
     *   reloadHook - Service reloading command
     *   Will be executed only on demand, by sending Reload signal to SvdService
     *
     *  @author dmilith
     */
    def reloadHook = config.reload


    /**
     *   validateHook - Performed right after configure on each application run
     *   Will throw exception when validation process wont pass
     *
     *  @author dmilith
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
        .replaceAll("SERVICE_PORT", if (config.staticPort == -1) s"${servicePort}" else s"${config.staticPort}")


    /**
     *   hookShot is a safe way to launch service hooks
     *
     *  @author dmilith
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
                        log.trace(s"MATCHED: ${hookName}")
                        if (config.reportAllDebugs)
                            accountManager ! Notify.Message(formatMessage(s"D:Done ${hookName} of service: ${config.name}"))

                    case "validate" =>
                        if (config.reportAllDebugs)
                            accountManager ! Notify.Message(formatMessage(s"D:Done ${hookName} of service: ${config.name}"))

                    case _ =>
                        if (config.reportAllInfos)
                            accountManager ! Notify.Message(formatMessage(s"D:Done ${hookName} of service: ${config.name}"))
                }

            } catch {
                case x: expectj.TimeoutException =>
                    val hk = hook.copy( commands = hook.commands.map { // map values for better message
                        replaceAllSpecialValues _
                    })
                    val msg = formatMessage(s"E:Hook ${hookName} of service: ${config.name} failed to pass expectations: CMD: '${hk.commands.mkString(" ")}', OUT: '${hk.expectStdOut}', ERR: '${hk.expectStdErr}'.")
                    log.error(msg)
                    if (config.reportAllErrors)
                        accountManager ! Notify.Message(msg)

                case x: Exception =>
                    val msg = formatMessage(s"F:Thrown exception in hook: ${hookName} of service: ${config.name} an exception content below:\n${x.getMessage} ${x.getStackTrace}")
                    log.error(msg)
                    if (config.reportAllErrors)
                        accountManager ! Notify.Message(msg)
            }
            // log.trace("after hookShot output: %s", shell.output)
        } else {
            log.trace(s"Command list empty in hook: ${hookName} of service ${config.name}")
        }
    }


    def stopScheduler {
        log.debug("Stopping scheduler for service.")
        accountManager ! SvdScheduler.StopJob(config.name)
    }


    override def preStart = {

        /* check for previous installation */
        log.debug(s"Looking for ${installIndicator} file to check software installation status")
        installIndicator.exists match {
            case true =>
                log.info(s"Service already installed: ${config.name}")

            case false =>
                log.info(s"Installing ${className}: ${config.name}")
                hookShot(installHook, "install")
                hookShot(configureHook, "configure")
        }
        hookShot(validateHook, "validate")

        // val pause = SvdConfig.serviceRestartPause / 2
        // log.debug("Waiting after validate hook for: %ss".format(pause/1000))
        // Thread.sleep(pause)

        if (config.autoStart) {
            log.info(s"Starting ${className}: ${config.name}")
            hookShot(startHook, "start")
            hookShot(afterStartHook, "afterStart")
        }

        // defining scheduler job
        if (!config.schedulerActions.isEmpty) {
            val amount = config.schedulerActions.length
            log.trace(s"Scheduler triggers defined: ${amount}")
            for (index <- 0 to amount - 1) {
                log.trace(s"Proceeding with index: ${index}")
                val action = config.schedulerActions(index)

                log.debug(s"Config scheduler actions for ${className} ${config.name} isn't empty.")
                try {
                    val name = config.name
                    val jobInstance = new ShellJob
                    val job = JobBuilder.newJob(jobInstance.getClass)
                        .withIdentity(s"${name}-${index}")
                        .build

                    // setting job data values:
                    val cronEntry = CronScheduleBuilder.cronSchedule(action.cronEntry)
                    val shellOperations = SvdShellOperations(commands = action.shellCommands.map {
                            elem => replaceAllSpecialValues(elem)
                        })
                    job.getJobDataMap.put("shellOperations", shellOperations)
                    job.getJobDataMap.put("account", account)
                    val trigger = TriggerBuilder.newTrigger
                        .withIdentity(s"${name}-${index}")
                        .startNow
                        .withSchedule(cronEntry)
                        .build

                    accountManager ! SvdScheduler.StartJob(name, job, trigger)

                } catch {
                    case e: ParseException =>
                        accountManager ! Notify.Message(formatMessage(s"E:${e}"))

                    case e: Throwable =>
                        accountManager ! Notify.Message(formatMessage(s"F:${e}"))
                }

            }
        }

    }


    def receive = {

        case User.ServiceAutostart =>
            touch(autostartFileLocation)
            val msg = formatMessage(s"I:Turned on autostart of: ${this}")
            log.debug(msg)
            accountManager ! Notify.Message(msg)


        case User.ServiceStatus =>
            val msg = formatMessage(s"I:${this}")
            log.debug(msg)
            accountManager ! Notify.Message(msg)
            sender ! s"${this}"


        case User.GetServicePort =>
            log.debug(s"Getting port of ${className}: ${config.name}:${servicePort}")
            sender ! servicePort


        case Notify.Ping =>
            log.debug(s"Ping from: ${this}")
            sender ! Notify.Pong


        /**
         *   Reload by default should be SIGHUP signal sent to process pid
         *
         *  @author dmilith
         */
        case Signal.Reload =>
            hookShot(validateHook, "validate")
            hookShot(reloadHook, "reload")
            sender ! ApiSuccess


        /**
         *   Explicit method to launch service
         *
         *  @author dmilith
         */
        case Signal.Run =>
            hookShot(startHook, "start")
            hookShot(afterStartHook, "afterStart")
            sender ! ApiSuccess


        /**
         *   Quit should be sent when we want to stop this service
         *
         *  @author dmilith
         */
        case Signal.Quit =>
            log.info(s"Got Quit in ${className}")
            context.unwatch(self)
            context.stop(self)
            sender ! ApiSuccess


        case ApiSuccess =>
            log.trace(s"ApiSuccess in ${className} from ${sender.getClass.getSimpleName}")

    }


    override def postStop {
        log.info(s"PostStop in ${className}: ${config.name}")
        hookShot(stopHook, "stop")
        hookShot(afterStopHook, "afterStop")

        stopScheduler
        val pause = SvdConfig.serviceRestartPause / 2
        log.debug(s"Waiting for scheduler stop for ${pause /1000} seconds")
        Thread.sleep(pause)

        shell.close
        log.info(s"Stopped ${className}: ${config.name}")
        super.postStop
    }


    override def toString = s"${className} name: ${config.name}. Uptime: ${secondsToHMS((JSystem.currentTimeMillis - uptime).toInt / 1000)}"

}
