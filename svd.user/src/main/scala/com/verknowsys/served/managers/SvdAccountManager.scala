package com.verknowsys.served.managers

import com.verknowsys.served._
import com.verknowsys.served.api._
import com.verknowsys.served.api.accountkeys._
import com.verknowsys.served.api.git._
import com.verknowsys.served.services._
import com.verknowsys.served.db.{DBServer, DBClient, DB}
import com.verknowsys.served.utils._
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.notifications._

import java.security.PublicKey
import akka.actor._
import akka.dispatch._
import akka.pattern.ask
import akka.remote._
import akka.util.Duration
import akka.util.Timeout
import akka.util.duration._
import akka.actor._


case class AccountKeys(keys: Set[AccessKey] = Set.empty, uuid: UUID = randomUUID) extends Persistent
object AccountKeysDB extends DB[AccountKeys]

/**
 * Account Manager - owner of all managers
 *
 * @author dmilith
 * @author teamon
 */
class SvdAccountManager(val account: SvdAccount, val headless: Boolean = false) extends SvdManager with SvdFileEventsReactor with Logging {

    // import akka.actor.OneForOneStrategy
    // import akka.actor.SupervisorStrategy._

    // override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 25, withinTimeRange = 1 minute) {
    //     // case _: Terminated               => Escalate
    //     case _: ArithmeticException      => Resume
    //     case _: NullPointerException     => Restart
    //     case _: IllegalArgumentException => Stop
    //     case _: Exception                => Escalate
    // }


    import com.verknowsys.served.utils.Events._

    class DBServerInitializationException extends Exception


    val userHomeDir = SvdConfig.userHomeDir / "%s".format(account.uid)
    // val preloadedServices = Source.fromFile(userHomeDir / "%d".format(account.uid) / SvdConfig.softwareDataDir / SvdConfig.defaultServicesFile).mkString.split(" ")

    val notificationsManager = context.actorOf(Props(new SvdNotificationCenter(account)).withDispatcher("svd-single-dispatcher"), "SvdNotificationCenter")
    val fem = context.actorOf(Props(new SvdFileEventsManager).withDispatcher("svd-single-dispatcher"), "SvdFileEventsManagerUser") // XXX: hardcode

    val accountsManager = context.actorFor("akka://%s@127.0.0.1:%d/user/SvdAccountsManager".format(SvdConfig.served, SvdConfig.remoteApiServerPort)) // XXX: hardcode
    val systemManager = context.actorFor("akka://%s@127.0.0.1:%d/user/SvdSystemManager".format(SvdConfig.served, SvdConfig.remoteApiServerPort)) // XXX: hardcode
    val sshd = context.actorFor("akka://%s@127.0.0.1:%d/user/SvdSSHD".format(SvdConfig.served, SvdConfig.remoteApiServerPort)) // XXX: hardcode


    override def preStart = {
        super.preStart

        log.info("Starting AccountManager (v%s) for uid: %s".format(SvdConfig.version, account.uid))

        if (headless) {
            // headless mode
            val headlessPort = account.uid + 1025 // choose one above 0:1024 range
            val dbPort = account.uid + 1030
            val websocketsPort = account.uid + 1035
            log.info("Headless mode assumes that machine services ports remains static for given user.")
            log.info("Headless port for this user is: %d. Database port: %d".format(headlessPort, dbPort))
            val dbServer = new DBServer(dbPort, userHomeDir / "%s.db".format(account.uid))
            val db = dbServer.openClient

            // NON DRY NON DRY NON DRY:
            val gitManager = context.actorOf(Props(new SvdGitManager(account, db, userHomeDir / "git")))
            val webManager = context.actorOf(Props(new SvdWebManager(account)).withDispatcher("svd-single-dispatcher"))

            context.watch(fem)
            context.watch(notificationsManager)
            context.watch(gitManager)
            context.watch(webManager)

            log.debug("Registering file events for 'watchfile'")
            registerFileEventFor(userHomeDir / "watchfile", Modified, uid = account.uid)

            log.debug("Registering file events for 'restart'")
            registerFileEventFor(userHomeDir / "restart", AttributesChanged, uid = account.uid)

            // Start the real work
            log.info("(NYI) Checking installed services")
            // TODO: Checking installed services

            log.debug("Becaming headless with started")
            context.become(started(db, dbServer, gitManager, notificationsManager, webManager, Nil))

            // import org.webbitserver._
            // import org.webbitserver.handler._
            // log.info("Spawning Webbit WebSockets Server")
            // val websocketsServer = WebServers.createWebServer(websocketsPort)
            //   .add("/livemonitor", new SvdWebSocketsHandler)
            //   // .add(new StaticFileHandler("/web"))
            //   .start.get
            // log.info("WebSockets Server running at " + websocketsServer.getUri)

            self ! User.SpawnServices // spawn userside services automatically

            // send availability of user to sshd manager
            // addDefaultAccessKey(db)
            // sshd ! InitSSHChannelForUID(account.uid)

        } else  {
            // normal mode
            log.debug("Getting database port from AccountsManager")
            (accountsManager ? Admin.GetPort) onSuccess {
                case dbPort: Int =>
                    log.debug("Got database port %d", dbPort)

                    // Start database server
                    val dbServer = new DBServer(dbPort, userHomeDir / "%s.db".format(account.uid))
                    val db = dbServer.openClient

                    // start managers
                    val gitManager = context.actorOf(Props(new SvdGitManager(account, db, userHomeDir / "git")))
                    val webManager = context.actorOf(Props(new SvdWebManager(account)).withDispatcher("svd-single-dispatcher"))

                    context.watch(fem)
                    context.watch(notificationsManager)
                    context.watch(gitManager)
                    context.watch(webManager)

                    // Start the real work
                    log.debug("Becaming started")
                    context.become(
                        started(db, dbServer, gitManager, notificationsManager, webManager, Nil))

                    accountsManager ! Admin.Alive(account) // registers current manager in accounts manager
                    self ! User.SpawnServices // spawn userside services

                    // send availability of user to sshd manager
                    addDefaultAccessKey(db)
                    sshd ! InitSSHChannelForUID(account.uid)

                case x =>
                    // sender ! Error("DB initialization error. Got param: %s".format(x))
                    throwException[DBServerInitializationException]("DB initialization error. Got param: %s".format(x))
            }
        }

    }

    // TODO: gather list of configurations from user config

    def receive = traceReceive {

        // case AuthorizeWithKey(key: PublicKey) =>
        //     log.debug("WTF? Not started manager!")

        case Success =>
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


    protected def readLogFile(serviceName: String, pattern: Option[String] = None) {
        import java.io._

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
                    }.take(20).reverse.mkString("\n")
                    notificationsManager ! Notify.Message(formatMessage("I:Last 20 lines of log: \n%s\n\nFull log here: http://verknowsys.com".format(content)))

                case None =>
                    val content = fileToString(access).split("\n").reverse.take(20).reverse.mkString("\n")
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


    private def started(db: DBClient, dbServer: DBServer, gitManager: ActorRef, notificationsManager: ActorRef, webManager: ActorRef, services: List[String] = Nil): Receive = traceReceive {

        case User.SpawnServices =>

            services.foreach {
                serviceName =>
                    // look for old services already started, and stop it:
                    def joinContext(withServices: List[String]) { // launch new service:
                        val serv = context.actorOf(Props(new SvdService(serviceName, account)), serviceName)
                        log.debug("Launching Service through SpawnServices: %s".format(serv))
                        context.watch(serv)
                        context.unbecome
                        log.debug("Currently maintained services: %s".format(withServices))
                        context.become(started(db, dbServer, gitManager, notificationsManager, webManager, withServices))
                    }
                    val currServ = context.actorFor("/user/SvdAccountManager/%s".format(serviceName))
                    (currServ ? Ping) onComplete {
                        case Right(Pong) =>
                            val msg = "Service already running: %s. Restarting".format(serviceName)
                            log.warn(msg)
                            notificationsManager ! Notify.Message(formatMessage("W:%s".format(msg)))
                            context.unwatch(currServ)
                            context.stop(currServ)
                            log.debug("Waiting for service shutdown hooks…")
                            Thread.sleep(SvdConfig.serviceRestartPause)
                            joinContext(services.filterNot(_ == serviceName))

                        case Left(exc) =>
                            log.debug("No alive actors found: %s".format(exc.getMessage))
                            joinContext(services)
                    }
            }

        case User.TerminateServices =>
            services.foreach {
                serviceName =>
                    log.debug("Terminating Service: %s".format(serviceName))
                    val servicesLeft = services.filterNot(_ == serviceName)
                    val serv = context.actorFor("/user/SvdAccountManager/%s".format(serviceName))
                    context.unwatch(serv)
                    context.stop(serv)
            }
            log.debug("Waiting for service shutdown hooks…")
            Thread.sleep(SvdConfig.serviceRestartPause)
            context.unbecome
            log.debug("Currently maintained services: %s".format(Nil))
            context.become(started(db, dbServer, gitManager, notificationsManager, webManager, Nil))

        case User.GetServices =>
            sender ! services

        case User.SpawnService(serviceName) =>
            log.debug("Spawning service: %s".format(serviceName))
            // look for old services already started, and stop it:
            def joinContext(withServices: List[String]) {
                // spawn new service with that name:
                try { // XXX: TODO: make sure it's safe
                    val serv = context.actorOf(Props(new SvdService(serviceName, account)), serviceName)
                    context.watch(serv)
                    context.unbecome
                    log.debug("Currently maintained services: %s".format(withServices))
                    context.become(started(db, dbServer, gitManager, notificationsManager, webManager, withServices))
                } catch {
                    case x: InvalidActorNameException =>
                        val msg = formatMessage("E:Invalid name exception (duplicate same service): %s".format(x.getMessage))
                        log.warn(msg)
                        notificationsManager ! Notify.Message(msg)
                }
            }
            val currServ = context.actorFor("/user/SvdAccountManager/%s".format(serviceName))
            (currServ ? Ping) onComplete {
                case Right(Pong) =>
                    val msg = "Service already running: %s. Restarting".format(serviceName)
                    log.warn(msg)
                    notificationsManager ! Notify.Message(formatMessage("W:%s".format(msg)))
                    context.unwatch(currServ)
                    context.stop(currServ)
                    log.debug("Waiting for service shutdown hooks…")
                    Thread.sleep(SvdConfig.serviceRestartPause)
                    joinContext(services.filterNot(_ == serviceName))

                case Left(exc) =>
                    log.debug("No alive service found: %s".format(exc.getMessage))
                    joinContext(serviceName :: services)
            }

        case User.TerminateService(name) =>
            log.debug("Stopping service: %s".format(name))
            val serv = context.actorFor("/user/SvdAccountManager/%s".format(name))
            context.unwatch(serv)
            context.stop(serv)
            val svces = services.filterNot{_.name == name}
            log.debug("Currently maintained services: %s".format(svces))
            context.unbecome
            context.become(started(db, dbServer, gitManager, notificationsManager, webManager, svces))

        case User.ReadLogFile(serviceName, pattern) =>
            log.debug("Reading log file for service: %s".format(serviceName))
            readLogFile(serviceName, pattern)

        case User.GetAccount =>
            sender ! account

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

        case System.RegisterDomain(domain) =>
            log.info("Registering domain: %s", domain)
            log.warn("NYI")
            sender ! Success

        case SvdFileEvent(path, flags) =>
            log.trace("REACT on file event on path: %s. Flags no: %s".format(path, flags))
            flags match {
                case Modified =>
                    log.trace("File event type: Modified")
                    // "INFO -- %s -- Performing %s of service: %s".format(currentHost, hookName, config.name)
                    notificationsManager ! Notify.Message(formatMessage("I:File event notification: Modified on path: %s.".format(path)))
                    gitManager ! Git.CreateRepository("somerepository")
                case Deleted =>
                    log.trace("File event type: Deleted")
                case Renamed =>
                    log.trace("File event type: Renamed")
                    // gitManager ! Shutdown
                case AttributesChanged =>
                    log.trace("File event type: AttributesChanged")
                    // gitManager ! RemoveRepository("somerepository")
                case Revoked =>
                    log.trace("File event type: Revoked")
                case x =>
                    log.trace("Got event: %s", x)

            }

        case Success =>
            log.debug("Received Success")

        case Terminated(ref) =>
            log.debug("Terminated service actor: %s".format(ref))
            context.unwatch(ref)

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
            if (headless) {
                x match {
                    case Admin.GetPort => // allow getting static port for headless account manager
                        import java.lang._

                        val numServices = services.length + 1

                        Thread.sleep(Math.abs(new scala.util.Random().nextInt % 100))
                        val randomPort = ((1024 + account.uid) + java.lang.System.currentTimeMillis % 10000).toInt// XXX: almost random in range of max 10000 service ports

                        sender ! Math.abs(randomPort)

                    case _ =>
                        val err = formatMessage("E:Forwarding to Accounts Manager can't work in headless mode.")
                        log.error(err)
                        notificationsManager ! Notify.Message(err)
                }
            } else {
                log.debug("Forwarding message: %s to Accounts Manager", x)
                accountsManager forward x
            }

        case x: System.Base =>
            if (headless) {
                log.debug("Forwarding message: %s to System Manager", x)
                systemManager forward x
            } else {
                val err = formatMessage("E:Forwarding to System Manager can't work in headless mode.")
                log.error(err)
                notificationsManager ! Notify.Message(err)
            }

        case x =>
            log.warn("Some unrecognized message catched in SAM: %s".format(x))

    }


    protected def accountKeys(db: DBClient) = {
        val ak = AccountKeysDB(db).headOption
        // log.debug("accountKeys: %s", ak)
        ak getOrElse AccountKeys()
    }


    addShutdownHook {
        log.warn("Forcing POST Stop in Account Manager")
        if (!headless)
            accountsManager ! Admin.Dead(account)

        postStop
    }


    override def postStop {
        // log.debug("Stopping database server and client")
        // db.close
        // dbServer.close

        log.info("Stopping services")
        (self ? User.TerminateServices) onSuccess {
            case _ =>
                log.info("All Services Terminated")
        } onFailure {
            case x =>
                log.error("TerminateServices fail: %s".format(x))
        }
        log.debug("Unbecoming AccountManager")
        context.unbecome
        log.debug("Terminated successfully")
        super.postStop
    }


    override def preRestart(reason: Throwable, message: Option[Any]) = {
        log.warn("preRestart caused by reason: %s and message: %s", reason, message)
        super.preRestart(reason, message)
    }


}
