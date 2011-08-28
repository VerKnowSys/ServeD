package com.verknowsys.served.managers

import com.verknowsys.served.services._
import com.verknowsys.served.LocalAccountsManager
import com.verknowsys.served.SvdConfig
import com.verknowsys.served.api.accountkeys._
import com.verknowsys.served.api.git._
import com.verknowsys.served.api._
import com.verknowsys.served.db.{DBServer, DBClient, DB}
import com.verknowsys.served.utils._
import com.verknowsys.served.systemmanager.native._

import akka.actor.Actor.{remote, actorOf, registry}
import akka.actor.{Actor, ActorRef}


case class AccountKeys(keys: Set[AccessKey] = Set.empty, uuid: UUID = randomUUID) extends Persistent
object AccountKeysDB extends DB[AccountKeys]

/**
 * Account Manager - owner of all managers
 *
 * @author teamon
 */
class SvdAccountManager(val account: SvdAccount) extends SvdExceptionHandler {

    class DBServerInitializationException extends Exception

    log.info("Starting AccountManager for uid: %s".format(account.uid))

    val homeDir = SvdConfig.userHomeDir / account.uid.toString
    val sh = new SvdShell(account)

    // Only for closing in postStop
    private var _dbServer: Option[DBServer] = None // XXX: Refactor
    private var _dbClient: Option[DBClient] = None // XXX: Refactor

    val _apps = actorOf(new SvdService(SvdUserServices.passengerConfig(account), account))


    log.info("Spawning applications of uid: %s".format(account.uid))
    // TODO: gather list of configurations from user config

    def receive = traceReceive {
        case Init =>
            log.info("SvdAccountManager received Init.")
            val psAll = SvdLowLevelSystemAccess.processList(false)
            log.debug("All user process IDs: %s".format(psAll.mkString(", ")))

            // Connect to database
            // Get port from pool
            log.debug("Getting database port from AccountsManager")
            (LocalAccountsManager !! GetPort) match {
                case Some(dbPort: Int) =>
                    log.debug("Got database port %d", dbPort)
                    // Start database server
                    val server = new DBServer(dbPort, SvdConfig.userHomeDir / "%s".format(account.uid) / "%s.db".format(account.uid))
                    val db = server.openClient

                    _dbServer = Some(server)
                    _dbClient = Some(db)

                    log.trace("Spawning user app: %s".format(_apps))
                    _apps.start
                    _apps !! Run /* temporary call due to lack of web interface */
                    _apps !! Reload /* temporary call due to lack of web interface */
                    self startLink _apps

                    // Start GitManager for this account
                    val gitManager = Actor.actorOf(new SvdGitManager(account, db, homeDir / "git"))
                    self startLink gitManager

                    self reply Success

                    // Start the real work
                    log.trace("Becaming started")
                    LocalAccountsManager ! Alive(account.uid)
                    become(started(db, gitManager))

                case _ =>
                    self reply Error
                    throw new DBServerInitializationException
            }

            // TODO:
            // new SvdService(account, "rails app x", SvdShellOperation("rails dupa" :: "cd dupa" :: "script/rails" :: Nil)).start
            // self reply Success
    }

    def started(db: DBClient, gitManager: ActorRef): Receive = traceReceive {
        case GetAccount =>
            self reply account

        case AuthorizeWithKey(key) =>
            log.trace("Trying to find key in account: %s", account)
            self reply accountKeys(db).keys.find(_.key == key).isDefined

        case ListKeys =>
            self reply accountKeys(db).keys

        case AddKey(key) =>
            val ak = accountKeys(db)
            db << ak.copy(keys = ak.keys + key)

        case RemoveKey(key) =>
            val ak = accountKeys(db)
            db << ak.copy(keys = ak.keys - key)

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
        _apps.stop
        sh.close
        _dbClient.foreach(_.close)
        _dbServer.foreach(_.close)
        super.postStop
    }

}
