/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.managers

import com.verknowsys.served._
import com.verknowsys.served.api._
import com.verknowsys.served.api.scheduler._
import com.verknowsys.served.api.accountkeys._
import com.verknowsys.served.api.git._
import com.verknowsys.served.services._
import com.verknowsys.served.db.{DBServer, DBClient, DB}
import com.verknowsys.served.utils._
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.notifications._

import org.apache.commons.io.FileUtils
import java.security.PublicKey
import akka.actor._
import akka.pattern.AskTimeoutException
import akka.pattern.ask
import akka.util
import akka.util.Timeout
import scala.util._
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

import scala.math._
import scala.util.Random
import org.quartz._
import org.quartz.impl._
import org.quartz.JobKey._
import org.quartz.impl.matchers._
import java.io.{File, FileNotFoundException}
import java.lang.{System => JSystem}


case class AccountKeys(keys: Set[AccessKey] = Set.empty, uuid: UUID = randomUUID) extends Persistent
object AccountKeysDB extends DB[AccountKeys]

class DBServerInitializationException extends Exception

case object SvdAccounts extends DB[SvdAccount]
case object SvdUserPorts extends DB[SvdUserPort]
case object SvdSystemPorts extends DB[SvdSystemPort]
case object SvdUserUIDs extends DB[SvdUserUID]
case object SvdUserDomains extends DB[SvdUserDomain]
case object SvdFileEventBindings extends DB[SvdFileEventBinding]


/**
 * Account Manager - User Side Manager, which has purpose to watch over all user side mechanisms and APIs
 *
 * @author dmilith
 * @author teamon
 */
class SvdAccountManager(val bootAccount: SvdAccount, val userBoot: ActorRef, val headless: Boolean = false) extends SvdManager with SvdFileEventsReactor with Logging {

    // import akka.actor.OneForOneStrategy
    // import akka.actor.SupervisorStrategy._

    // override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 25, withinTimeRange = 1 minute) {
    //     // case _: Terminated               => Escalate
    //     case _: ArithmeticException      => Resume
    //     case _: NullPointerException     => Restart
    //     case _: IllegalArgumentException => Stop
    //     case _: Exception                => Escalate
    // }


    import Events._

    val scheduler = StdSchedulerFactory.getDefaultScheduler
    val userHomeDir = SvdConfig.userHomeDir / "%s".format(bootAccount.uid)

    val notificationsManager = context.actorOf(Props(new SvdNotificationCenter(bootAccount)).withDispatcher("svd-single-dispatcher"), "SvdNotificationCenter")
    val fem = context.actorOf(Props(new SvdFileEventsManager).withDispatcher("svd-single-dispatcher"), "SvdFileEventsManagerUser")
    val moshManager = context.actorOf(Props(new SvdMoshManager(bootAccount)).withDispatcher("svd-single-dispatcher"), "SvdMoshManager")

    val accountsManager = context.actorFor("akka://%s@%s:%d/user/SvdAccountsManager".format(SvdConfig.served, SvdConfig.remoteApiServerHost, SvdConfig.remoteApiServerPort))
    val systemManager = context.actorFor("akka://%s@%s:%d/user/SvdSystemManager".format(SvdConfig.served, SvdConfig.remoteApiServerHost, SvdConfig.remoteApiServerPort))
    val sshd = context.actorFor("akka://%s@%s:%d/user/SvdSSHD".format(SvdConfig.served, SvdConfig.remoteApiServerHost, SvdConfig.remoteApiServerPort))


    def displayAdditionalInformation(db: DBClient) {
        log.debug("User File Event Bindings registered in Account database: %s", SvdFileEventBindings(db).mkString(", "))
        log.debug("User ports registered in Account database: %s".format(SvdUserPorts(db).mkString(", ")))
        log.debug("System ports registered in Account database: %s".format(SvdSystemPorts(db).mkString(", ")))
        log.debug("User uids registered in Account database: %s".format(SvdUserUIDs(db).mkString(", ")))
        log.debug("User domains registered in Account database: %s".format(SvdUserUIDs(db).mkString(", ")))
        log.warn("File Events Flags and numerical values: Modified: %d, Deleted: %d, Renamed: %d, AttributesChanged: %d, Revoked: %d, Linked: %d, All: %d", Modified, Deleted, Renamed, AttributesChanged, Revoked, Linked, All)
    }


    override def preStart = {
        super.preStart

        log.info("Starting Quartz Scheduler")
        scheduler.start

        log.debug("Account Manager base folder checks in progress")
        checkOrCreateDir(userHomeDir / SvdConfig.softwareDataDir)
        checkOrCreateDir(userHomeDir / SvdConfig.webConfigDir)
        checkOrCreateDir(userHomeDir / SvdConfig.defaultUserIgnitersDir)


        log.info("Starting AccountManager (v%s) for uid: %s".format(SvdConfig.version, bootAccount.uid))

        val port = SvdAccountUtils.randomFreePort
        log.debug("Got database port %d", port)

        // Start database server
        val dbServer = new DBServer(port, userHomeDir / "%s.db".format(bootAccount.uid))
        val db = dbServer.openClient
        val utils = new SvdAccountUtils(db)

        // load real account
        val account = SvdAccounts(db).find{_.uid == bootAccount.uid}.getOrElse {
            db << bootAccount.copy(userName = "Unnamed Local User")
            bootAccount
        } // load first account with given uid from local user database. Preveil same uuid on every boot.


        // start managers
        val gitManager = context.actorOf(Props(new SvdGitManager(account, db, userHomeDir / "git")), "SvdGitManager")
        val webManager = context.actorOf(Props(new SvdWebManager(account)).withDispatcher("svd-single-dispatcher"), "SvdWebManager")

        context.watch(fem)
        context.watch(notificationsManager)
        context.watch(gitManager)
        context.watch(webManager)

        // Start the real work

        displayAdditionalInformation(db)

        log.debug("Becaming started")
        context.become(
            started(account, db, dbServer, gitManager, notificationsManager, webManager, utils))

        if (!headless)
            accountsManager ! Admin.Alive(account) // registers current manager in accounts manager

        self ! User.SpawnServices // spawn userside services

        // send availability of user to sshd manager
        addDefaultAccessKey(db)
        sshd ! InitSSHChannelForUID(account.uid)
    }


    // TODO: gather list of configurations from user config

    def receive = traceReceive {

        case ApiSuccess =>
            log.debug("Got success")

        case Terminated(ref) =>
            log.debug("Terminated service actor: %s".format(ref))
            context.unwatch(ref)

        case msg: Notify.Base =>
            log.trace("Forwarding notification to Notification Center")
            notificationsManager forward msg

        case x =>
            val m = "SvdAccountManager already become zombie stage but received message: %s".format(x)
            log.warn("%s".format(m))
            // sender ! Error(m)

    }


    protected def addDefaultAccessKey(db: DBClient) = {
        def defaultUserPublicKey = scala.io.Source.fromURL(getClass.getResource("/defaultUserKey.pub")).getLines.mkString("\n")
        val key = KeyUtils.load(defaultUserPublicKey)
        if (accountKeys(db).keys.find(_.key == key.get).isDefined) {
            log.trace("Already defined default AccessKey.")
        } else {
            log.trace("Adding default AccessKey.")
            self ! AddKey(AccessKey(name = SvdConfig.defaultUserName, key = key.get))
        }
    }


    protected def readLogFile(account: SvdAccount, serviceName: String, pattern: Option[String] = None) { // , amount: Option[Int] = None
        val arg = serviceName.capitalize
        val logPath = SvdConfig.userHomeDir / "%d".format(account.uid) / SvdConfig.softwareDataDir / arg / SvdConfig.defaultServiceLogFile

        try {
            val access = new File(logPath)
            val logFileSize = access.length.toInt
            log.debug("Reading tail of log file: %s with %d bytes.", logPath, logFileSize)
            pattern match {
                case Some(patt) =>
                    val content = fileToString(access).split("\n").reverse.filter{
                        _.contains(patt)
                    }.take(20).reverse.mkString("\n") // XXX: hardcode

                    // TODO: upload log to private gist or something

                    notificationsManager ! Notify.Message(formatMessage("I:Last 20 lines of log: \n%s\n\nFull log here: http://paster.verknowsys.com/SOME-SHA".format(content)))

                case None =>
                    val content = fileToString(access).split("\n").reverse.take(20).reverse.mkString("\n") // XXX: hardcode
                    notificationsManager ! Notify.Message(formatMessage("I:Last 20 lines of log: \n%s\n\nFull log here: http://verknowsys.com".format(content)))
            }
            log.trace("Forcing GC after log show")
            Runtime.getRuntime.gc

        } catch {
            case e: FileNotFoundException =>
                val msg = formatMessage("E:Log file not available: %s".format(e))
                log.error(msg)
                notificationsManager ! Notify.Message(msg)

            case x: Exception =>
                val msg = formatMessage("E:Exception happened: %s".format(x))
                log.error(msg)
                notificationsManager ! Notify.Message(msg)
        }
    }


    /**
     *  Cleans autostart mark from services software data dir.
     *
     *  @author dmilith
     */
    def cleanServicesAutostart {
        val servicesLocationDir = SvdConfig.userHomeDir / "%d".format(bootAccount.uid) / SvdConfig.softwareDataDir
        listDirectories(servicesLocationDir).map {
            dir =>
                val file = new java.io.File(dir.toString / SvdConfig.serviceAutostartFile)
                if (file.exists) {
                    log.debug("Removing autostart file: %s", file)
                    rm_r(file.toString)
                }
        }
    }


    /**
     *  Load autostart marks from services software data dir.
     *
     *  @author dmilith
     */
    def loadServicesList = {
        val servicesLocationDir = SvdConfig.userHomeDir / "%d".format(bootAccount.uid) / SvdConfig.softwareDataDir
        log.debug("Found services dir: %s".format(servicesLocationDir))
        listDirectories(servicesLocationDir).map {
            dir =>
                if (new File(dir.toString / ".autostart_service").exists) { // XXX: hardcode
                    log.debug("Found autostart for %s".format(dir))
                    dir.toString.split("/").last
                } else {
                    log.debug("No autostart for %s".format(dir))
                    ""
                }
        }.filterNot(_.isEmpty)
    }


    private def started(account: SvdAccount, db: DBClient, dbServer: DBServer, gitManager: ActorRef, notificationsManager: ActorRef, webManager: ActorRef, utils: SvdAccountUtils): Receive = traceReceive {


        case Shutdown =>
            if (!headless) {
                log.info("Sending Death Announcement to Account Manager")
                accountsManager ! Admin.Dead(account)
            }


        case SvdScheduler.StartJob(name, job, trigger) =>
            log.debug("Starting schedule job named: %s for service: %s".format(name, sender))
            scheduler.scheduleJob(job, trigger)


        case SvdScheduler.StopJob(name) =>
            log.debug("Stopping scheduled jobs named: %s for service: %s".format(name, sender))
            for (index <- 0 to SvdConfig.maxSchedulerDefinitions) { // XXX: hacky.. it's better to figure out how to get list of defined jobs from scheduler..
                try {
                    scheduler.deleteJob(jobKey("%s-%d".format(name, index)))
                } catch {
                    case e: Exception =>
                        log.debug(s"Exception when deleting job from user scheduler: ${e}")
                }
            }


        case msg: Maintenance.Base =>
            log.debug("Forwarding Maintenance message to UserBoot")
            userBoot forward msg


        case User.CloneIgniterForUser(igniterName, userIgniterNewName) => // #13
            try {
                val igniterFile = new File(SvdConfig.defaultSoftwareTemplatesDir / igniterName + SvdConfig.defaultSoftwareTemplateExt)
                val userIgniterName = new File(userHomeDir / SvdConfig.defaultUserIgnitersDir / (
                    userIgniterNewName match {
                        case Some(nameSet) =>
                            nameSet
                        case None =>
                            igniterName
                    }
                ))
                val fullIgniterName = userIgniterName + SvdConfig.defaultSoftwareTemplateExt
                log.debug(s"Creating user side igniter from base igniter file: ${igniterFile} to destination: ${fullIgniterName}")
                FileUtils.copyFile(igniterFile, fullIgniterName, false) // NOTE: false => don't copy attributes
                sender ! ApiSuccess
            } catch {
                case e: Exception =>
                    sender ! Error("Exception occured: %s".format(e))
            }


        case Admin.RegisterAccount(userName) => // #14
            sender ! Error("Not yet implemented.")


        case User.SpawnServices => // #10
            val listOfServices = loadServicesList
            log.debug("List of all services stored: %s".format(listOfServices.mkString(", ")))
            listOfServices.foreach {
                serviceName =>
                    // look for old services already started, and stop it:
                    def joinContext {
                        val serv = context.actorOf(Props(new SvdService(serviceName, account)), "Service-%s".format(serviceName))
                        log.debug("Launching Service through SpawnServices: %s".format(serv))
                        context.watch(serv)
                    }
                    val currServ = context.actorFor("/user/SvdAccountManager/Service-%s".format(serviceName))
                    log.trace("Pinging service: %s".format(currServ))
                    (currServ ? Notify.Ping) onComplete {
                        case Success(_) =>
                            val msg = "Service already running: %s.".format(serviceName)
                            log.warn(msg)
                            notificationsManager ! Notify.Message(formatMessage("W:%s".format(msg)))
                            // currServ ! Quit
                            // log.debug("Waiting for service shutdown hooks…")
                            // Thread.sleep(SvdConfig.serviceRestartPause)
                            // joinContext

                        case Failure(exc) =>
                            log.debug("No alive actors found: %s".format(exc.getMessage))
                            joinContext
                    }
            }
            sender ! ApiSuccess


        case User.GetStoredServices => // #4
            val listOfServices = loadServicesList
            notificationsManager ! Notify.Message(formatMessage("I:%s".format(listOfServices.mkString(", "))))
            sender ! """{"message": "Stored services", "content": [%s]}""".format(listOfServices.map{ c => "\"" +c+ "\"" }.mkString(", "))


        case User.TerminateServices => // #5
            log.info("Terminating all services…")
            context.actorSelection("../SvdAccountManager/Service-*") ! Signal.Quit
            sender ! ApiSuccess


        case User.StoreServices => // #6
            cleanServicesAutostart
            context.actorSelection("../SvdAccountManager/Service-*") ! User.ServiceAutostart
            sender ! ApiSuccess


        case User.GetRunningServices =>
            context.actorSelection("../SvdAccountManager/Service-*") ! User.ServiceStatus


        case User.SpawnService(serviceName) => // #7
            log.debug("Spawning service: %s".format(serviceName))

            def joinContext { // look for old services already started, and stop it:
                try { // XXX: TODO: make sure it's safe
                    val serv = context.actorOf(Props(new SvdService(serviceName, account)), "Service-%s".format(serviceName)) // spawn new service with that name:
                    context.watch(serv)
                } catch {
                    case x: InvalidActorNameException =>
                        val msg = formatMessage("E:Invalid name exception (duplicate same service): %s. Causing Restart of Service.".format(x.getMessage))
                        log.warn(msg)
                        notificationsManager ! Notify.Message(msg)

                    case x: Exception =>
                        val msg = formatMessage("E:Something nasty happened with service: %s".format(x.getMessage))
                        log.warn(msg)
                        notificationsManager ! Notify.Message(msg)
                }
            }
            val currServ = context.actorFor("/user/SvdAccountManager/Service-%s".format(serviceName))
            (currServ ? Notify.Ping) onComplete {
                case Success(anyPong) =>
                    val msg = "Service already running: %s. Restarting".format(serviceName)
                    log.warn(msg)
                    notificationsManager ! Notify.Message(formatMessage("W:%s".format(msg)))
                    context.unwatch(currServ)
                    context.stop(currServ)
                    log.debug("Waiting for service shutdown hooks…")
                    Thread.sleep(SvdConfig.serviceRestartPause)
                    joinContext

                case Failure(exc) =>
                    log.debug("No alive service found: %s".format(exc.getMessage))
                    joinContext
            }
            sender ! ApiSuccess


        case User.TerminateService(name) => // #8
            log.debug("Stopping service: %s".format(name))
            val serv = context.actorFor("/user/SvdAccountManager/Service-%s".format(name))
            context.unwatch(serv)
            context.stop(serv)
            sender ! ApiSuccess


        case User.ShowAvailableServices => // #9
            log.debug("Showing available services definitions")
            val availableSvces = (
                listFiles(SvdConfig.defaultSoftwareTemplatesDir) ++ listFiles(userHomeDir / SvdConfig.defaultUserIgnitersDir))
                .filter{_.getName.endsWith(SvdConfig.defaultSoftwareTemplateExt)}.map{_.getName.split("/").last}.mkString(",").replaceAll(SvdConfig.defaultSoftwareTemplateExt, "") // XXX: replace with some good regexp
            notificationsManager ! Notify.Message("Available Services: " + availableSvces)
            sender ! """{"message": "Available services", "content": [%s]}""".format(availableSvces.split(",").map{ c => "\"" +c+ "\"" }.mkString(", "))


        case User.ReadLogFile(serviceName, pattern) =>
            log.debug("Reading log file for service: %s".format(serviceName))
            readLogFile(account, serviceName, pattern)


        case User.StoreUserDomain(domain) => // internal call
            log.info("Storing user domain: %s", domain)
            utils.registerUserDomain(domain)


        case User.GetRegisteredDomains => // #3
            log.debug("Displaying registerd domains.")
            val domains = SvdUserDomains(db)
            log.info("GetRegisteredDomains: %s", domains.mkString(", "))
            sender ! """{"message": "Domain list", "content": [%s]}""".format(domains.map{c => "\"" +c.name+ "\"" }.mkString(", "))


        case User.GetServicePort(serviceName) => // #12
            log.debug("Asking service %s for port it's using.".format(serviceName))
            val s = sender
            val currServ = context.actorFor("/user/SvdAccountManager/Service-%s".format(serviceName))
            (currServ ? User.GetServicePort) onComplete {
                case Success(port) =>
                    s ! """{"message": "Port gathered successfully.", "content": [%d]}""".format(port)
                case Failure(exc) =>
                    s ! Error("Service port unavailable!")
            }

        case User.GetServiceStatus(serviceName) => // #11
            val s = sender
            val currServ = context.actorFor("/user/SvdAccountManager/Service-%s".format(serviceName))
            (currServ ? User.ServiceStatus) onComplete {
                case Success(content) =>
                    s ! """{"message": "Service: %s. %s", "status": 0}""".format(serviceName, content)

                case Failure(x) =>
                    x match {
                        case x: AskTimeoutException =>
                            s ! Error("Service refused to answer. Installation is in progress or service's not started.")
                        case x: Exception =>
                            s ! Error("Critical error: %s".format(x))

                    }
            }


        case User.CreateFileWatch(fileName, flags, serviceName) => // #15
            val path = userHomeDir / fileName
            log.debug("Creating file watch on file: %s. Creating full path: %s", fileName, path)
            SvdFileEventBindings(db).find{_.absoluteFilePath == path} match {
                case Some(x) =>
                    log.trace("x contains: %s", x)
                    log.info("Already registered File Event for file (or directory): %s. Ignoring trigger registering.", path)

                case None =>
                    registerFileEventFor(path, flags)
                    db << SvdFileEventBinding(path, serviceName, flags)
                    log.info("Created file watch on file %s", path)
                    sender ! ApiSuccess
            }


        case User.DestroyFileWatch(fileName) => // #16
            val path = userHomeDir / fileName
            log.debug("Destroying file watch on file: %s.", path)

            SvdFileEventBindings(db).find{_.absoluteFilePath == path}.map {
                binding =>
                    log.info("Destroying file watch and binding for: %s", path)
                    val currServ = context.actorFor("/user/SvdAccountManager/Service-%s".format(binding.serviceName))
                    (currServ ? User.ServiceStatus) onComplete {
                        case Success(content) =>
                            log.debug("Found FEM Triggered Service: %s", binding.serviceName)
                            db ~ binding // remove record from user account db
                            log.info("Removed binding for file %s", path)
                            context.stop(currServ)

                        case Failure(xc) =>
                            log.trace("FEM Triggered Service Not (yet) started.")
                            db ~ binding
                    }
            }
            log.debug("Unregistering File Events for path: %s", path)
            log.trace("Currently registered and stored bindings: %s", SvdFileEventBindings(db).mkString(", "))
            fem ! SvdUnregisterFileEvent(self) // unregister event watch
            sender ! ApiSuccess


        case User.GetUserPorts =>
            log.debug("Getting User ports for account: %s", account)
            val portsListFormatted = SvdUserPorts(db).map{_.number}.mkString(",")
            sender ! s"""{"message": "Stored services", "content": [${portsListFormatted}]}"""


        case User.RegisterUserPort =>
            if (headless) {
                val newFreeLocalPort = SvdAccountUtils.randomFreePort
                log.debug("Registering user port: %s", newFreeLocalPort)
                db << SvdUserPort(number = newFreeLocalPort)
                sender ! s"""{"message": "Registered port for headless account", "content": "${newFreeLocalPort}"}"""

            } else { // "Connected to SvdRoot mode"
                log.trace("Got User.RegisterUserPort")

                SvdUserPorts(db).find{_.number == 12345} getOrElse { // XXX: HACK
                    if (portAvailable(12345)) {
                        log.trace("Registering port: %s", 12345)
                        db << SvdUserPort(number = 12345) // XXX: HACK
                    }
                }

                systemManager forward User.RegisterUserPort // XXX: hack. It must be done on root side, but an utils is required to perform read and checks of all /SystemUsers/Security/*.json

                sender ! """{"message": "Port registered: %s", "content": [%s]}""".format(12345, 12345)
            }


        case User.RemoveAllUserPorts =>
            log.debug("Removing All registered ports for account: %s", account)
            SvdUserPorts(db).map{
                portRecord =>
                    log.trace("Removing port: %s from user account.", portRecord.number)
                    db ~ portRecord
            }
            sender ! ApiSuccess


        case User.MoshAuth =>
            log.debug("Forwarding MoshAuth call to SvdMoshManager")
            moshManager forward User.MoshAuth


        case AuthorizeWithKey(key) =>
            log.debug("Trying to find key in account: %s", account)
            sender ! accountKeys(db).keys.find(_.key == key).isDefined


        case ListKeys =>
            sender ! accountKeys(db).keys


        case AddKey(key) =>
            val ak = accountKeys(db)
            // log.debug("Adding key to user database: %s".format(ak))
            db << ak.copy(keys = ak.keys + key)


        case RemoveKey(key) =>
            val ak = accountKeys(db)
            db << ak.copy(keys = ak.keys - key)


        case SvdFileEvent(path, flags) =>
            // at this point we know that trigger was shot and we need to match file to name of service and we're done
            synchronized {
                log.trace("REACT on file event on path: %s. Flags no: %s".format(path, flags))
                SvdFileEventBindings(db).toList.map {
                    binding =>
                        log.debug("Iterating through File Event Bindings. Currently: %s", binding)
                        log.trace("%s vs %s,  %s vs %s,  svc: %s", path, binding.absoluteFilePath, flags, binding.flags, binding.serviceName)
                        if ((path == binding.absoluteFilePath) &&  // this must be exactly same path
                            (flags < binding.flags)) { // FIXME: XXX: NOT SURE it's ok, but flags must be at least same right?
                            log.info("Launching trigger service for file: %s (if not already started)", path)
                            val currServ = context.actorFor("/user/SvdAccountManager/Service-%s".format(binding.serviceName))
                            (currServ ? Notify.Ping) onComplete {
                                case Success(anyPong) => // it seems that service is already started
                                    log.info("Triggered Service already running")

                                case Failure(ex) => // timeout probably?
                                    log.trace("CurrServ exception: %s", ex)
                                    val service = context.actorOf(Props(new SvdService(binding.serviceName, account)), "Service-%s".format(binding.serviceName))
                                    context.watch(service)
                            }
                        }
                }
            }


        case ApiSuccess =>
            log.debug("Received ApiSuccess")

        case Terminated(ref) =>
            log.debug("Terminated service actor: %s".format(ref))
            context.unwatch(ref)

        case msg: Security.Base =>
            log.trace("Forwarding Security message to System Manager")
            systemManager forward msg

        // redirect user notification messages directly to notification center
        case msg: Notify.Base =>
            log.trace("Forwarding notification to Notification Center")
            notificationsManager forward msg

        case Some(x) =>
            x match {
                case repo: Repository =>
                    log.trace("Forwarding Git message to Git Manager")
                    gitManager forward x

                case x =>
                    log.error("Ambigous message: %s".format(x))
            }

        case msg: Git.Base =>
            log.trace("Forwarding Git message to Git Manager")
            gitManager forward msg

        case x: Events.Base =>
            log.trace("Forwarding Events message to File Event Manager")
            fem forward x

        case x: Admin.Base =>
            log.debug("Forwarding message: %s to Accounts Manager", x)
            accountsManager forward x


        case System.GetPort => // allow getting static port for headless account manager
            if (headless) {
                Thread.sleep(abs(new Random().nextInt % 100))
                val randomPort = ((1024 + account.uid) + JSystem.currentTimeMillis % 10000).toInt// XXX: almost random in range of max 10000 service ports
                sender ! abs(randomPort)
            } else {
                val port = utils.randomUserPort
                utils.registerUserPort(port)
                sender ! port
            }


        case x: System.Base =>
            if (!headless) {
                log.debug("Forwarding message: %s to System Manager", x)
                systemManager forward x
            } else {
                val err = formatMessage("E:Forwarding to System Manager can't work in headless mode.")
                log.error(err)
                notificationsManager ! Notify.Message(err)
            }

        case x =>
            log.warn("Some unrecognized message catched in SAM: %s".format(x))

        // TODO: do user registration
        // case Admin.RegisterAccount(name) =>
        //     log.trace("Registering default account if not present")
        //     if (name == SvdConfig.defaultUserName) {
        //         if (!userUIDRegistered(SvdConfig.defaultUserUID)) {
        //             registerUserAccount(SvdConfig.defaultUserName, SvdConfig.defaultUserUID) // XXX: hardcoded
        //         }
        //         sender ! ApiSuccess
        //     } else {
        //         val userUID = randomUserUid
        //         log.debug("Registering account with name: %s and uid: %d".format(name, userUID))
        //         registerUserAccount(name, userUID)
        //         sender ! ApiSuccess
        //     }

    }


    protected def accountKeys(db: DBClient) = {
        val ak = AccountKeysDB(db).headOption
        // log.debug("accountKeys: %s", ak)
        ak getOrElse AccountKeys()
    }


    addShutdownHook {
        log.warn("Forcing POST Stop in Account Manager")
        postStop
    }


    override def postStop {
        // log.debug("Stopping database server and client")
        // db.close
        // dbServer.close

        log.info("Stopping services")
        val ts = (self ? User.TerminateServices)
        ts onSuccess {
            case _ =>
                log.info("All Services Terminated")
        }
        ts onFailure {
            case x =>
                log.error("TerminateServices fail: %s".format(x))
        }

        log.info("Terminating Mosh Sessions")
        val ms = (moshManager ? Shutdown)
        ms onSuccess {
            case _ =>
                log.info("All Mosh Sessions Terminated")
        }
        ms onFailure {
            case x =>
                log.error(s"TerminateMoshSessions fail: ${x}")
        }
        context.stop(moshManager)

        log.info("Terminating File Events Manager")
        context.stop(fem)

        log.info("Terminating Notification center")
        context.stop(notificationsManager)

        log.info("Terminating Core Scheduler")
        scheduler.shutdown

        log.info("Terminating Account Manager")
        val shutdown = (self ? Shutdown)
        shutdown onSuccess {
            case _ =>
                context.unbecome
                log.info("Everything Terminated Successfully")
        }
        shutdown onFailure {
            case x =>
                // context.unbecome
                log.error("Failure while Termination process: ${x}")
        }
        context.stop _
    }


    override def preRestart(reason: Throwable, message: Option[Any]) = {
        log.warn("preRestart caused by reason: %s and message: %s", reason, message)
        super.preRestart(reason, message)
    }


}
