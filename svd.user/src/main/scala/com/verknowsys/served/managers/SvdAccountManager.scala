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
 * @author teamon
 */
class SvdAccountManager(val account: SvdAccount) extends SvdExceptionHandler {

    class DBServerInitializationException extends Exception


    log.info("Starting AccountManager (v%s) for uid: %s".format(SvdConfig.version, account.uid))

    val homeDir = SvdConfig.userHomeDir / account.uid.toString
    val sh = new SvdShell(account)
    val accountsManager = context.actorFor("akka://%s@127.0.0.1:%d/user/SvdAccountsManager".format(SvdConfig.served, SvdConfig.remoteApiServerPort)) // XXX: hardcode
    val sshd = context.actorFor("akka://%s@127.0.0.1:%d/user/SvdSSHD".format(SvdConfig.served, SvdConfig.remoteApiServerPort)) // XXX: hardcode


    // Only for closing in postStop
    private var _dbServer: Option[DBServer] = None // XXX: Refactor
    private var _dbClient: Option[DBClient] = None // XXX: Refactor

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



    log.info("Spawning applications of uid: %s".format(account.uid))
    // TODO: gather list of configurations from user config

    def receive = traceReceive {
        case Init =>
            log.info("SvdAccountManager received Init.")

            // log.info("Spawning user databases: %s".format(_dbs))
            // _dbs.start
            // _dbs !! Run /* temporary call due to lack of web interface */
            // self startLink _dbs

            // Connect to database
            // Get port from pool
            log.debug("Getting database port from AccountsManager")

            (accountsManager ? GetPort) onSuccess {
                case dbPort: Int =>
                    log.debug("Got database port %d", dbPort)
                    // Start database server
                    val server = new DBServer(dbPort, SvdConfig.userHomeDir / "%s".format(account.uid) / "%s.db".format(account.uid))
                    val db = server.openClient

                    log.info(SvdUserServices.newPhpWebAppEntry("Php", SvdUserDomain("deldaphp", false), account))

                    // _dbServer = Some(server)
                    // _dbClient = Some(db)

                    // log.info("Spawning user app: %s".format(_apps))
                    // _apps.start
                    // _apps !! Run /* temporary call due to lack of web interface */
                    // _apps !! Reload /* temporary call due to lack of web interface */
                    // self startLink _apps



                    // Start GitManager for this account
                    val gitManager = context.actorOf(Props(new SvdGitManager(account, db, homeDir / "git")))
                    (gitManager ? Init) onSuccess {
                        case _ =>
                            log.debug("Setting log watch on git manager.")
                            context.watch(gitManager)
                    }

                    // Start the real work
                    log.debug("Becaming started")
                    accountsManager ! Alive(account.uid)
                    context.become(started(db, gitManager))
                    self ! Init

                    // little hacky but gives ability to load default key
                    // def defaultUserPublicKey = scala.io.Source.fromURL(getClass.getResource("/defaultUserKey.pub")).getLines.mkString("\n")
                    // val key = KeyUtils.load(defaultUserPublicKey)
                    // self ! AddKey(AccessKey(name = "guest", key = key.get))
                    sender ! Success

                case x =>
                    sender ! Error("DB initialization error. Got param: %s".format(x))
                    SvdUtils.throwException[DBServerInitializationException]("DB initialization error. Got param: %s".format(x))
            }

        // case AuthorizeWithKey(key: PublicKey) =>
        //     log.debug("WTF? Not started manager!")


        case Success =>
            log.debug("Got success")


        case x =>
            val m = "Unknown SvdAccountManager message: %s".format(x)
            log.warn("%s".format(m))
            sender ! Error(m)

    }

    def started(db: DBClient, gitManager: ActorRef): Receive = traceReceive {
        // case GetUserProcessList =>
        //     val psAll = SvdLowLevelSystemAccess.processList(false)
        //     log.debug("All user process IDs: %s".format(psAll.mkString(", ")))
        case Init =>
            log.debug("Sending init to SSHD manager")
            // send availability of user to sshd manager
            sshd ! InitSSHChannelForUID(account.uid)


        case GetAccount =>
            sender ! account

        case AuthorizeWithKey(key) =>
            log.debug("Trying to find key in account: %s", account)
            sender ! accountKeys(db).keys.find(_.key == key).isDefined

        case ListKeys =>
            sender ! accountKeys(db).keys

        case AddKey(key) =>
            val ak = accountKeys(db)
            db << ak.copy(keys = ak.keys + key)

        case RemoveKey(key) =>
            val ak = accountKeys(db)
            db << ak.copy(keys = ak.keys - key)

        case Success =>
            log.debug("Become started with success")

        case msg: git.Base =>
            gitManager forward msg
    }

    protected def accountKeys(db: DBClient) = {
        val ak = AccountKeysDB(db).headOption
        log.debug("accountKeys: %s", ak)
        ak getOrElse AccountKeys()
    }


    override def postStop {
        log.debug("Executing postStop for user svd UID: %s".format(account.uid))
        // _apps.stop
        // _dbs.stop
        sh.close
        _dbClient.foreach(_.close)
        _dbServer.foreach(_.close)
        super.postStop
    }

}
