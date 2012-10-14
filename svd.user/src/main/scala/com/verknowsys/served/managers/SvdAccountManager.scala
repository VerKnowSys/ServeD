package com.verknowsys.served.managers

import com.verknowsys.served.services._
// import com.verknowsys.served.LocalAccountsManager
import com.verknowsys.served.SvdConfig
import com.verknowsys.served.api.accountkeys._
import com.verknowsys.served.api.git._
import com.verknowsys.served.api._
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
class SvdAccountManager(val account: SvdAccount) extends SvdExceptionHandler with SvdFileEventsReactor with SvdUtils {

    import com.verknowsys.served.utils.events._

    class DBServerInitializationException extends Exception

    log.info("Starting AccountManager (v%s) for uid: %s".format(SvdConfig.version, account.uid))

    val homeDir = SvdConfig.userHomeDir / account.uid.toString
    val sh = new SvdShell(account)
    val accountsManager = context.actorFor("akka://%s@127.0.0.1:%d/user/SvdAccountsManager".format(SvdConfig.served, SvdConfig.remoteApiServerPort)) // XXX: hardcode
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
        log.info("SvdAccountManager is loading.")
        log.debug("Registering file events for 'watchfile'")
        registerFileEventFor(userHomePath / "watchfile", All, uid = account.uid)

        log.debug("Getting database port from AccountsManager")
        (accountsManager ? GetPort) onSuccess {
            case dbPort: Int =>
                log.debug("Got database port %d", dbPort)
                // Start database server
                val dbServer = new DBServer(dbPort, userHomePath / "%s.db".format(account.uid))
                val db = dbServer.openClient

                // log.info(SvdUserServices.newPhpWebAppEntry("Php", SvdUserDomain("deldaphp", false), account))

                // log.info("Spawning user app: %s".format(_apps))
                // _apps.start
                // _apps !! Run /* temporary call due to lack of web interface */
                // _apps !! Reload /* temporary call due to lack of web interface */
                // self startLink _apps

                val notificationsManager = context.actorOf(Props(new SvdNotificationCenter(account)))
                val gitManager = context.actorOf(Props(new SvdGitManager(account, db, homeDir / "git")))
                val webManager = context.actorOf(Props(new SvdWebManager(account)))

                // Start the real work
                log.debug("Becaming started")
                context.become(started(db, dbServer, gitManager, notificationsManager, webManager))
                accountsManager ! Alive(account.uid)

                // send availability of user to sshd manager
                addDefaultAccessKey(db)
                sshd ! InitSSHChannelForUID(account.uid)

            case x =>
                sender ! Error("DB initialization error. Got param: %s".format(x))
                throwException[DBServerInitializationException]("DB initialization error. Got param: %s".format(x))
        }

    }

    // TODO: gather list of configurations from user config

    def receive = traceReceive {

        // case AuthorizeWithKey(key: PublicKey) =>
        //     log.debug("WTF? Not started manager!")

        case Success =>
            log.debug("Got success")


        case x =>
            val m = "Unknown SvdAccountManager message: %s".format(x)
            log.warn("%s".format(m))
            sender ! Error(m)

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


    private def started(db: DBClient, dbServer: DBServer, gitManager: ActorRef, notificationsManager: ActorRef, webManager: ActorRef): Receive = traceReceive {
        // case GetUserProcessList =>
        //     val psAll = SvdLowLevelSystemAccess.processList(false)
        //     log.debug("All user process IDs: %s".format(psAll.mkString(", ")))

        // case Shutdown =>
        //     log.warn("Shutting down Account manager of: %s", account)
        //     db.close
        //     dbServer.close

        case GetAccount =>
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

        case Success =>
            log.debug("Received Success")

        case SvdFileEvent(path, flags) =>
            log.trace("REACT on file event on path: %s. Flags no: %s".format(path, flags))
            flags match {
                case Modified =>
                    log.trace("File event type: Modified")
                    notificationsManager ! Notify.Message("File event notification: Modified on path: %s on host: %s".format(path, currentHost.getHostName))
                    gitManager ! CreateRepository("somerepository")
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

        // case Terminated(ref) => // XXX: it seems to be super fucked up way to maintain actors. Use supervision Luke!
        //     context.unwatch(ref)
        //     context.stop(ref)
        //     (accountsManager ? GetPort) onSuccess {
        //         case dbPort: Int =>
        //             log.debug("Actor restart pending")
        //             // val server = new DBServer(dbPort, userHomePath / "%s.db".format(account.uid))
        //             val db = dbServer.openClient
        //             val gm = context.actorOf(Props(new SvdGitManager(account, db, homeDir / "git")))

        //         case x =>
        //             log.error("Wtf?: %s", x)

        //     } onFailure {
        //         case x =>
        //             log.debug("Failure after Terminated signal")
        //     }
            // context.stop(ref)
            // val gitManager = context.actorOf(Props(new SvdGitManager(account, db, homeDir / "git")))
            // (gitManager ? Init) onSuccess {
            //     case _ =>
            //         log.debug("Setting log watch on git manager.")
            //         context.watch(gitManager)
            // }


        // redirect user notification messages directly to notification center
        case msg: Notify.Message =>
            log.trace("Forwarding notification to Notification Center")
            notificationsManager forward msg

        case msg: git.Base =>
            gitManager forward msg
    }


    protected def accountKeys(db: DBClient) = {
        val ak = AccountKeysDB(db).headOption
        // log.debug("accountKeys: %s", ak)
        ak getOrElse AccountKeys()
    }


    override def postStop {
        sh.close
        context.unbecome
        log.debug("Executing postStop for user svd UID: %s".format(account.uid))
        super.postStop
    }


    override def preRestart(reason: Throwable) = {
        log.warn("preRestart caused by reason: %s", reason)
        super.preRestart(reason)
    }


}
