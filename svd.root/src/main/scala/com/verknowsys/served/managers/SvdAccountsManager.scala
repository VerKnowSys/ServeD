package com.verknowsys.served.managers


import com.verknowsys.served._
import com.verknowsys.served.db._
import com.verknowsys.served.SvdConfig
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.events.SvdFileEvent
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.utils.signals.SvdPOSIX._
import com.verknowsys.served.systemmanager.managers._
import com.verknowsys.served.api._
import com.verknowsys.served.api.pools._
import com.verknowsys.served.services._

import akka.actor._
import akka.actor._
import akka.dispatch._
import akka.pattern.ask
import akka.remote._
import akka.util.Duration
import akka.util.Timeout
import akka.util.duration._

import scala.io.Source
import java.io._
import scala.util._


case object SvdAccounts extends DB[SvdAccount]
case object SvdUserPorts extends DB[SvdUserPort]
case object SvdSystemPorts extends DB[SvdSystemPort]
case object SvdUserUIDs extends DB[SvdUserUID]

class SvdAccountUtils(db: DBClient) extends Logging {
    /**
     *  @author dmilith
     *
     *   randomUserPort is a helper function to be used with SvdUserPort in API
     */
    def randomUserPort: Int = {
        val rnd = new Random(System.currentTimeMillis)
        val port = SvdPools.userPortPool.start + rnd.nextInt(SvdPools.userPortPool.end - SvdPools.userPortPool.start)
        if (SvdUtils.portAvailable(port) && !userPortRegistered(port)) {
            port
        } else
            randomUserPort
    }


    /**
     *  @author dmilith
     *
     *   randomSystemPort is a helper function to be used with SvdSystemPort in API
     */
    def randomSystemPort: Int = {
        val rnd = new Random(System.currentTimeMillis)
        val port = SvdPools.systemPortPool.start + rnd.nextInt(SvdPools.systemPortPool.end - SvdPools.systemPortPool.start)
        if (SvdUtils.portAvailable(port) && !systemPortRegistered(port)) {
            port
        } else
            randomSystemPort
    }


    /**
     *  @author dmilith
     *
     *   randomUserUid is a helper function to be used with SvdUserUID in API
     */
    def randomUserUid: Int = {
        val rnd = new Random(System.currentTimeMillis)
        val uid = SvdPools.userUidPool.start + rnd.nextInt(SvdPools.userUidPool.end - SvdPools.userUidPool.start)
        if (!userUIDRegistered(uid)) {
            uid
        } else
            randomUserUid
    }


    /**
     *  @author dmilith
     *
     *   creates akka configuration file for given user
     */
    def createAkkaUserConfIfNotExistant(uid: Int, userManagerPort: Int) = {
        val configFile = SvdConfig.userHomeDir / "%d".format(uid) / "akka.conf"

        if (!new File(configFile).exists) {
            log.debug("Akka config: %s not found. Generating default one", configFile)

            def using[A <: {def close(): Unit}, B](param: A)(f: A => B): B =
                try { f(param) } finally { param.close() }

            def writeToFile(fileName: String, data: String) =
                using (new FileWriter(fileName)) {
                    fileWriter => fileWriter.write(data)
            }
            val defaultConfig = Source.fromURL(
                getClass.getResource(
                    SvdConfig.defaultUserAkkaConf
                )
            ).getLines.mkString("\n").replaceAll("USER_NETTY_PORT", "%d".format(userManagerPort))
            writeToFile(configFile, defaultConfig)
        } else {
            log.debug("Akka config found: %s", configFile)
        }
    }


    /**
     *  @author dmilith
     *
     *   registers user with given name and uid number in svd database
     */
    def registerUserAccount(name: String, uid: Int): Unit = {
        val userManagerPort = randomUserPort
        val userHomeDir = SvdConfig.userHomeDir / "%d".format(uid)

        def performChecks(managerPort: Int = userManagerPort) {
            log.trace("Performing user registration checks and making missing directories")
            SvdUtils.checkOrCreateDir(userHomeDir)
            SvdUtils.chown(userHomeDir, uid)
            createAkkaUserConfIfNotExistant(uid, managerPort)
        }

        if (!userUIDRegistered(uid)) {
            log.trace("Generated user manager port: %d for account with uid: %d", userManagerPort, uid)
            if (!userPortRegistered(userManagerPort)) {
                registerUserPort(userManagerPort)
                log.trace("Registered user manager port: %s", userManagerPort)
            } else {
                log.trace("Registering once again cause of port dup: %s", userManagerPort)
                registerUserAccount(name, uid)
            }
            registerUserUID(uid)
            performChecks()
            log.debug("Writing account data of uid: %d", uid)
            db << SvdAccount(userName = name, uid = uid, accountManagerPort = userManagerPort)
        } else {
            val userAccount = SvdAccounts(db).filter{_.uid == uid}.head
            val userManagerPort = userAccount.accountManagerPort
            log.trace("User already registered with manager port: %d, but still validating existance of akka user file and home directory: %s", userManagerPort, userHomeDir)
            performChecks(userManagerPort)
        }
    }


    /**
     *  @author dmilith
     *
     *   registers user UID with given number and name in svd database
     */
    def registerUserUID(num: Int) =
        db << SvdUserUID(number = num)


    /**
     *  @author dmilith
     *
     *   registers user port with given number in svd database
     */
    def registerUserPort(num: Int) =
        db << SvdUserPort(number = num)


    /**
     *  @author dmilith
     *
     *   registers system port with given number in svd database
     */
    def registerSystemPort(num: Int) =
        db << SvdSystemPort(number = num)


    /**
     *  @author dmilith
     *
     *   returns true if user port is registered in svd database
     */
    def userPortRegistered(num: Int) =
        if (SvdUserPorts(db)(_.number == num).isEmpty)
            false
        else
            true


    /**
     *  @author dmilith
     *
     *   returns true if system port is registered in svd database
     */
    def systemPortRegistered(num: Int) =
        if (SvdSystemPorts(db)(_.number == num).isEmpty)
            false
        else
            true


    /**s
     *  @author dmilith
     *
     *   returns true if system uid is registered in svd database
     */
    def userUIDRegistered(num: Int) =
        if (SvdUserUIDs(db)(_.number == num).isEmpty)
            false
        else
            true


}

// object SvdAccountsManager // extends GlobalActorRef(actorOf[SvdAccountsManager])

class SvdAccountsManager extends SvdExceptionHandler with SvdFileEventsReactor {

    import events._

    val server = new DBServer(SvdConfig.remoteAccountServerPort, SvdConfig.systemHomeDir / SvdConfig.coreSvdAccountsDatabaseName)
    val db = server.openClient

    val rootAccount = SvdAccount("root", uid = 0)
    val svdAccountUtils = new SvdAccountUtils(db)
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
    SvdUtils.addShutdownHook {
        log.warn("Got termination signal")
        log.info("Stopping spawned user workers")
        SvdAccounts(db).foreach{
            account =>
                val pidFile = SvdConfig.userHomeDir / "%d".format(account.uid) / "%d.pid".format(account.uid)
                log.trace("PIDFile: %s".format(pidFile))
                if (new java.io.File(pidFile).exists) {
                    log.trace("Reading VM pid.")
                    val pid = Source.fromFile(pidFile).mkString.trim.toInt
                    log.debug("Client VM PID to be killed: %d".format(pid))
                    SvdUtils.kill(pid, SIGTERM)
                    log.debug("Client VM PID file to be deleted: %s".format(pidFile))
                    SvdUtils.rm_r(pidFile)
                } else {
                    log.warn("File not found: %s".format(pidFile))
                }
        }

        // removing also pid file of root core of svd:
        val corePid = SvdConfig.systemHomeDir / SvdConfig.rootPidFile
        log.debug("Cleaning core pid file: %s with content: %s".format(corePid, Source.fromFile(corePid).mkString))
        SvdUtils.rm_r(corePid)
        log.info("Shutting down SvdAccountsManager")
        db.close
        server.close
    }


    def receive = {
        case Init =>
            log.debug("SvdAccountsManager received Init. Running default task..")

            // registerFileEventFor(SvdConfig.systemHomeDir, Modified)


            // log.info("Spawning Coreginx")
            // coreginx.start
            // coreginx ! Run

            self ! RespawnAccounts
            sender ! Success


        case Shutdown =>
            log.debug("Got Shutdown")
            sender ! Shutdown

        case RespawnAccounts =>
            log.trace("Respawning accounts")
            respawnUsersActors
            sender ! Success

        case RegisterAccount(name) =>
            log.trace("Registering default account if not present")
            if (name == SvdConfig.defaultUserName) {
                if (!userUIDRegistered(SvdConfig.defaultUserUID)) {
                    registerUserAccount(name, SvdConfig.defaultUserUID) // XXX: hardcoded
                    sender ! Success
                }
            } else {
                val userUID = randomUserUid
                log.debug("Registering account with name: %s and uid: %d", name, userUID)
                registerUserAccount(name, userUID)
                sender ! Success
            }

        case GetAccount(uid) =>
            val account = SvdAccounts(db)(_.uid == uid).headOption
            log.debug("GetAccount(%d): %s", uid, account)
            sender ! account

        case GetAccountByName(name) =>
            val account = SvdAccounts(db)(_.userName == name).headOption
            log.debug("GetAccountByName(%s): %s", name, account)
            sender ! account

        // case SetAccountManager(uid) =>
        //     // SvdGlobalRegistry.ActorManagers.values += (uid -> self)
        //     log.info("User worker spawned successfully and mounted in SvdGlobalRegistry. Current store: %s", SvdGlobalRegistry.ActorManagers.values)
        //     sender ! Success

        // case GetAccountManager(uid) =>
        //     log.trace("GetAccountManager(%d)", uid)
        //     sender ! (SvdGlobalRegistry.ActorManagers.values get uid getOrElse AccountNotFound)

        case Alive(uid) =>
            // sender ! Error("Not yet implemented")
            // add uid manager to list of active managers?
            log.trace("UID %d is alive", uid)
            sender ! Success

        case GetPort =>
            sender ! randomUserPort

        case Success =>
            log.debug("Got success")


        case x: Any =>
            log.warn("%s has received unknown signal: %s".format(this.getClass, x))
            sender ! Error("Unknown signal %s".format(x))

    }


    private def respawnUsersActors {
        userAccounts.foreach{
            account =>
                // TODO: add routine to respawn only non spawned/new accounts? Currently it's handled by kickstart
                log.warn("Spawning account: %s".format(account))
                new SvdShell(account).exec(new SvdShellOperation(SvdConfig.kickApp + " " + account.uid))
        }
    }


    /**
    * @author dmilith
    *
    *   Reads accounts from account database
    */
    protected def userAccounts = SvdAccounts(db).toList


}
