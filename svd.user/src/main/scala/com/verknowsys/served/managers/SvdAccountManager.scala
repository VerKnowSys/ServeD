package com.verknowsys.served.managers

import com.verknowsys.served.SvdConfig
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
class SvdAccountManager(val account: SvdAccount, val headless: Boolean = false) extends SvdManager with SvdFileEventsReactor {
    // TODO: implement headless mode

    // import akka.actor.OneForOneStrategy
    // import akka.actor.SupervisorStrategy._

    // override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 25, withinTimeRange = 1 minute) {
    //     // case _: Terminated               => Escalate
    //     case _: ArithmeticException      => Resume
    //     case _: NullPointerException     => Restart
    //     case _: IllegalArgumentException => Stop
    //     case _: Exception                => Escalate
    // }


    import com.verknowsys.served.utils.events._

    class DBServerInitializationException extends Exception


    val homeDir = SvdConfig.userHomeDir / account.uid.toString
    val accountsManager = context.actorFor("akka://%s@127.0.0.1:%d/user/SvdAccountsManager".format(SvdConfig.served, SvdConfig.remoteApiServerPort)) // XXX: hardcode
    val systemManager = context.actorFor("akka://%s@127.0.0.1:%d/user/SvdSystemManager".format(SvdConfig.served, SvdConfig.remoteApiServerPort)) // XXX: hardcode
    val notificationsManager = context.actorOf(Props(new SvdNotificationCenter(account)), "SvdNotificationCenter")
    val sshd = context.actorFor("akka://%s@127.0.0.1:%d/user/SvdSSHD".format(SvdConfig.served, SvdConfig.remoteApiServerPort)) // XXX: hardcode
    val userHomePath = SvdConfig.userHomeDir / "%s".format(account.uid)

    // Only for closing in postStop
    // private var _dbServer: Option[DBServer] = None // XXX: Refactor
    // private var _dbClient: Option[DBClient] = None // XXX: Refactor

    // lazy val _passenger = new SvdService(
    //     SvdUserServices.rackWebAppConfig(
    //         account,
    //         domain = SvdUserDomain("delda") // NOTE: it's also tells about app root dir set to /Users/501/WebApps/delda
    //     ),
    //     account
    // )

    // lazy val _postgres = new SvdService(
    //     SvdUserServices.postgresDatabaseConfig(
    //         account
    //     ),
    //     account
    // )

    // val _apps =
    //     try {
    //         // actorFor("/user/app/passenger")
    //     } catch {
    //         case e: Throwable =>
    //             log.error("EXCPT in %s".format(e))
    //             // 2011-09-09 21:12:09 - dmilith - TODO: FIXME: PENDING: make notifications about eceptions to user
    //             // Actor.actorOf(new SvdService(new SvdServiceConfig("Noop"), account)) // 2011-09-09 20:27:13 - dmilith - HACK: empty actor
    //     }

    // val _dbs =
    //     try {
    //         // actorOf(_postgres)
    //     } catch {
    //         case e: Throwable =>
    //             log.error("EXCPT in %s".format(e))
    //             // 2011-09-09 21:12:09 - dmilith - TODO: FIXME: PENDING: make notifications about eceptions to user
    //             // Actor.actorOf(new SvdService(new SvdServiceConfig("Noop"), account)) // 2011-09-09 20:27:13 - dmilith - HACK: empty actor
    //     }

    override def preStart = {
        super.preStart
        log.info("Starting AccountManager (v%s) for uid: %s".format(SvdConfig.version, account.uid))

        log.debug("Registering file events for 'watchfile'")
        registerFileEventFor(userHomePath / "watchfile", All, uid = account.uid)

        if (headless) {
            // headless mode
            val headlessPort = account.uid + 1024 // choose one above 0:1024 range
            val dbPort = account.uid + 1025
            log.info("Headless mode assumes that machine services ports remains static for given user.")
            log.info("Headless port for this user is: %d. Database port: %d".format(headlessPort, dbPort))
            val dbServer = new DBServer(dbPort, userHomePath / "%s.db".format(account.uid))
            val db = dbServer.openClient


            // NON DRY NON DRY NON DRY:

            val gitManager = context.actorOf(Props(new SvdGitManager(account, db, homeDir / "git")))
            val webManager = context.actorOf(Props(new SvdWebManager(account)).withDispatcher("svd-single-dispatcher"))

            context.watch(notificationsManager)
            context.watch(gitManager)
            context.watch(webManager)

            val nginx = context.actorOf(Props(new SvdService("Nginx", account, notificationsManager, self)), "Nginx")
            val redis = context.actorOf(Props(new SvdService("Redis", account, notificationsManager, self)), "Redis")
            // val postgres = context.actorOf(Props(new SvdService("Postgresql", account, notificationsManager, self)), "Postgresql")
            val memcached = context.actorOf(Props(new SvdService("Memcached", account, notificationsManager, self)), "Memcached")

            // Start the real work
            log.info("(NYI) Checking installed services")
            // TODO: Checking installed services
            val services = (nginx :: redis :: memcached :: Nil) // :: postgres

            log.debug("Becaming headless with started")
            context.become(started(db, dbServer, gitManager, notificationsManager, webManager, services))

            self ! User.SpawnServices // spawn userside services

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
                    val dbServer = new DBServer(dbPort, userHomePath / "%s.db".format(account.uid))
                    val db = dbServer.openClient

                    // start managers
                    val gitManager = context.actorOf(Props(new SvdGitManager(account, db, homeDir / "git")))
                    val webManager = context.actorOf(Props(new SvdWebManager(account)).withDispatcher("svd-single-dispatcher"))

                    context.watch(notificationsManager)
                    context.watch(gitManager)
                    context.watch(webManager)

                    // user services start from this point:
                    val nginx = context.actorOf(Props(new SvdService("Nginx", account, notificationsManager, self)), "Nginx")
                    val redis = context.actorOf(Props(new SvdService("Redis", account, notificationsManager, self)), "Redis")
                    // val postgres = context.actorOf(Props(new SvdService("Postgresql", account, notificationsManager, self)), "Postgresql")
                    val memcached = context.actorOf(Props(new SvdService("Memcached", account, notificationsManager, self)), "Memcached")

                    // Start the real work
                    log.info("(NYI) Checking installed services")
                    // TODO: Checking installed services
                    val services = (nginx :: redis :: memcached :: Nil) // :: postgres


                    log.debug("Becaming started")
                    context.become(
                        started(db, dbServer, gitManager, notificationsManager, webManager, services))

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

        case Terminated(ref) => // XXX: it seems to be super fucked up way to maintain actors. Use supervision Luke!
            log.debug("! Terminated service actor: %s".format(ref))
            context.unwatch(ref)
            context.stop(ref)

        case x =>
            val m = "SvdAccountManager is in turned off stage but still receives message: %s".format(x)
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


    private def started(db: DBClient, dbServer: DBServer, gitManager: ActorRef, notificationsManager: ActorRef, webManager: ActorRef, services: List[ActorRef] = Nil): Receive = traceReceive {

        case User.TerminateServices =>
            services.foreach {
                service =>
                    log.debug("Terminating Service: %s".format(service))
                    context.stop(service)
            }
            sender ! Success

        case User.GetServices =>
            sender ! services

        case User.SpawnServices =>
            services.foreach {
                service =>
                    log.debug("Launching Service through SpawnServices: %s".format(service))
                    context.watch(service)
            }

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
                    notificationsManager ! Notify.Message("File event notification: Modified on path: %s on host: %s".format(path, currentHost.getHostName))
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

        // redirect user notification messages directly to notification center
        case msg: Notify.Message =>
            log.trace("Forwarding notification to Notification Center")
            notificationsManager forward msg

        case msg: Git.Base =>
            log.trace("Forwarding Git message to Git Manager")
            gitManager forward msg

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
                        val err = "Forwarding to Accounts Manager can't work in headless mode."
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
                val err = "Forwarding to System Manager can't work in headless mode."
                log.error(err)
                notificationsManager ! Notify.Message(err)
            }

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
        log.info("Stopping services")
        (self ? User.TerminateServices) onSuccess {
            case _ =>
                log.debug("Terminated successfully")
                context.unbecome
                super.postStop

        } onFailure {
            case x =>
                log.error("TerminateServices fail: %s".format(x))
        }
    }


    override def preRestart(reason: Throwable, message: Option[Any]) = {
        log.warn("preRestart caused by reason: %s and message: %s", reason, message)
        super.preRestart(reason, message)
    }


}
