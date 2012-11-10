package com.verknowsys.served.managers


import com.verknowsys.served._
import com.verknowsys.served.db._
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.Events._
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.utils.signals.SvdPOSIX._
import com.verknowsys.served.systemmanager.managers._
import com.verknowsys.served.api._
import com.verknowsys.served.api.pools._
import com.verknowsys.served.services._

import scala.io.Source
import akka.actor._
import akka.actor._
import akka.dispatch._
import akka.pattern.ask
import akka.remote._
import akka.util.Duration
import akka.util.Timeout
import akka.util.duration._


case object SvdAccounts extends DB[SvdAccount]
case object SvdUserPorts extends DB[SvdUserPort]
case object SvdSystemPorts extends DB[SvdSystemPort]
case object SvdUserUIDs extends DB[SvdUserUID]


/**
 *  ServeD Accounts Manager
 *
 *  @author dmilith
 *
 */
class SvdAccountsManager extends SvdManager with SvdFileEventsReactor with Logging {

    val server = new DBServer(SvdConfig.remoteAccountServerPort, SvdConfig.systemHomeDir / SvdConfig.coreSvdAccountsDatabaseName)
    val db = server.openClient

    val rootAccount = SvdAccount("root", uid = 0)
    val svdAccountUtils = new SvdAccountUtils(db)

    import Events._
    import svdAccountUtils._


    log.info("SvdAccountsManager (v%s) is loading".format(SvdConfig.version))

    // log.info("Registering Coreginx")
    // val coreginx = actorOf(new SvdService(SvdRootServices.coreginxConfig(), rootAccount))

    log.debug("User accounts registered in Account database: %s".format(SvdAccounts(db).mkString(", ")))
    log.debug("User ports registered in Account database: %s".format(SvdUserPorts(db).mkString(", ")))
    log.debug("System ports registered in Account database: %s".format(SvdSystemPorts(db).mkString(", ")))
    log.debug("User uids registered in Account database: %s".format(SvdUserUIDs(db).mkString(", ")))


    // private val accountManagers = scala.collection.mutable.Map[Int, ActorRef]() // UID => AccountManager ref

    // protected val systemPasswdFilePath = SvdConfig.systemPasswdFile // NOTE: This must be copied into value to use in pattern matching
    addShutdownHook {
        log.warn("Got termination signal. Unregistering file events")
        unregisterFileEvents(self)

        log.info("Stopping spawned user workers")
        userAccounts.foreach{
            account =>
                val pidFile = SvdConfig.userHomeDir / "%d".format(account.uid) / "%d.pid".format(account.uid)
                log.trace("PIDFile: %s".format(pidFile))
                if (new java.io.File(pidFile).exists) {
                    val pid = Source.fromFile(pidFile).getLines.toList.head.trim.toInt
                    log.debug("Client VM PID to be killed: %d".format(pid))

                    // XXX: TODO: define death watch daemon:
                    kill(pid)

                    log.debug("Client VM PID file to be deleted: %s".format(pidFile))
                    rm_r(pidFile)
                } else {
                    log.warn("File not found: %s".format(pidFile))
                }
        }
        log.info("All done.")
        // postStop
    }


    override def preStart = {
        super.preStart
        log.debug("SvdAccountsManager is starting. Respawning per user Account Managers")
        respawnUsersActors
    }


    def awareOfUserManagers(accountsAlive: List[SvdAccount]): Receive = {

        case User.GetAccount(uid) =>
            val account = SvdAccounts(db)(_.uid == uid).headOption
            log.debug("GetAccount(%d): %s", uid, account)
            sender ! account

        case User.GetAccountByName(name) =>
            val account = SvdAccounts(db)(_.userName == name).headOption
            log.debug("GetAccountByName(%s): %s", name, account)
            sender ! account

        case Admin.RegisterAccount(name) =>
            log.trace("Registering default account if not present")
            if (name == SvdConfig.defaultUserName) {
                if (!userUIDRegistered(SvdConfig.defaultUserUID)) {
                    registerUserAccount(SvdConfig.defaultUserName, SvdConfig.defaultUserUID) // XXX: hardcoded
                }
                sender ! Success
            } else {
                val userUID = randomUserUid
                log.debug("Registering account with name: %s and uid: %d".format(name, userUID))
                registerUserAccount(name, userUID)
                sender ! Success
            }

        // case SetAccountManager(uid) =>
        //     // SvdGlobalRegistry.ActorManagers.values += (uid -> self)
        //     log.info("User worker spawned successfully and mounted in SvdGlobalRegistry. Current store: %s", SvdGlobalRegistry.ActorManagers.values)
        //     sender ! Success

        // case GetAccountManager(uid) =>
        //     log.trace("GetAccountManager(%d)", uid)
        //     sender ! (SvdGlobalRegistry.ActorManagers.values get uid getOrElse AccountNotFound)

        case Admin.Alive(account) =>
            // sender ! Error("Not yet implemented")
            // add uid manager to list of active managers?
            context.become(
                awareOfUserManagers(account :: accountsAlive))
            log.info("Becoming aware of alive account: %s", account)
            log.debug("Alive accounts: %s".format(account :: accountsAlive))

        case Admin.Dead(account) =>
            val accountsWithoutThisOne = accountsAlive.filterNot{_.uuid == account.uuid}
            context.become(
                awareOfUserManagers(accountsWithoutThisOne))
            sender ! Success
            log.info("Becoming aware of dead account: %s", account)
            log.debug("Alive accounts: %s".format(accountsWithoutThisOne))

        case Admin.RespawnAccounts =>
            log.trace("Respawning accounts")
            respawnUsersActors
            sender ! Success

        case Admin.GetPort =>
            val port = randomUserPort
            // registerUserPort(port)
            sender ! port

        case SvdFileEvent(path, flags) =>
            log.trace("REACT on file event on path: %s. Flags no: %s".format(path, flags))
            flags match {
                case Modified =>
                    log.trace("File event type: Modified")
                case Deleted =>
                    log.trace("File event type: Deleted")
                case Renamed =>
                    log.trace("File event type: Renamed")
                case AttributesChanged =>
                    log.trace("File event type: AttributesChanged")
                case Revoked =>
                    log.trace("File event type: Revoked")
                case x =>
                    log.trace("Got event: %s", x)
            }

        case Success =>
            log.debug("Got success")

        case x: Any =>
            log.warn("%s has received unknown signal: %s".format(this.getClass, x))
            // sender ! Error("Unknown signal %s".format(x))

    }


    def receive = { awareOfUserManagers(Nil) }


    private def respawnUsersActors {
        userAccounts.foreach{
            account =>

                // TODO: add routine to respawn only non spawned/new accounts? Currently it's handled by kickstart
                // val authKeysFile = SvdConfig.userHomeDir / "%s".format(account.uid) / ".ssh" / "authorized_keys"
                // log.debug("Registering file event routine for %s", authKeysFile)
                // registerFileEventFor(authKeysFile, All) // Modified | Deleted | Renamed | AttributesChanged

                log.trace("Spawning account: %s".format(account))
                new SvdShell(account).exec(new SvdShellOperations(SvdConfig.kickApp + " " + account.uid))
        }
    }


    /**
    * @author dmilith
    *
    *   Reads accounts from account database
    */
    protected def userAccounts = SvdAccounts(db).toList


    override def postStop = {
        log.debug("Accounts Manager postStop. Closing database")
        db.close
        server.close
        super.postStop
    }

}
