package com.verknowsys.served.managers


import com.verknowsys.served.db._
import com.verknowsys.served.SvdConfig
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.events.SvdFileEvent
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.systemmanager.managers._
import com.verknowsys.served.api._
import com.verknowsys.served.api.pools._
import com.verknowsys.served.services._

import akka.actor.{Actor, ActorRef}
import akka.actor.Actor.{remote, actorOf, registry}
import scala.io.Source
import java.io.File
import scala.util._


case object SvdAccounts extends DB[SvdAccount]
case object SvdUserPorts extends DB[SvdUserPort]
case object SvdSystemPorts extends DB[SvdSystemPort]
case object SvdUserUIDs extends DB[SvdUserUID]

class SvdAccountUtils(db: DBClient) {
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
     *   registers user UID with given number in svd database
     */
    def registerUserAccount(uid: Int) = {
        registerUserUID(uid)
        db << SvdAccount(uid = uid)
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

object SvdAccountsManager extends GlobalActorRef(actorOf[SvdAccountsManager])

class SvdAccountsManager extends SvdExceptionHandler with SvdFileEventsReactor {

    import events._

    val server = new DBServer(SvdConfig.remoteAccountServerPort, SvdConfig.systemHomeDir / SvdConfig.coreSvdAccountsDatabaseName)
    val db = server.openClient

    val rootAccount = SvdAccount("root", uid = 0)
    val svdAccountUtils = new SvdAccountUtils(db)
    import svdAccountUtils._

    log.info("SvdAccountsManager (v%s) is loading".format(SvdConfig.version))

    log.info("Registering Coreginx")
    val coreginx = actorOf(new SvdService(SvdRootServices.coreginxConfig(), rootAccount))

    log.debug("User accounts registered in Account database: %s".format(SvdAccounts(db).mkString(", ")))
    log.debug("User ports registered in Account database: %s".format(SvdUserPorts(db).mkString(", ")))
    log.debug("System ports registered in Account database: %s".format(SvdSystemPorts(db).mkString(", ")))
    log.debug("User uids registered in Account database: %s".format(SvdUserUIDs(db).mkString(", ")))


    private val accountManagers = scala.collection.mutable.Map[Int, ActorRef]() // UID => AccountManager ref

    // protected val systemPasswdFilePath = SvdConfig.systemPasswdFile // NOTE: This must be copied into value to use in pattern matching

    override def postStop {
        super.postStop

        log.debug("Stopping Coreginx")
        coreginx.stop

        log.info("Stopping spawned user workers")
        SvdAccounts(db).foreach{
            account =>
                val pidFile = SvdConfig.userHomeDir / "%d".format(account.uid) / "%d.pid".format(account.uid)
                log.trace("PIDFile: %s".format(pidFile))
                try {
                    val pid = Source.fromFile(pidFile).mkString
                    log.trace("Client VM PID to be killed: %s".format(pid))
                    new SvdShell(account).exec(new SvdShellOperation(
                        """
                        /bin/kill -INT %s
                        /bin/rm %s
                        """.format(pid, pidFile)
                    ))
                } catch {
                    case e: java.io.FileNotFoundException =>
                        log.warn("User pid file not found in %s!".format(pidFile))
                }
        }

        // removing also pid file of root core of svd:
        val corePid = SvdConfig.systemHomeDir / SvdConfig.rootPidFile
        log.trace("Cleaning core pid file: %s with content: %s".format(corePid, Source.fromFile(corePid).mkString))
        new SvdShell(rootAccount).exec(new SvdShellOperation("/bin/rm %s".format(corePid))) // XXX: hardcoded

        log.trace("Invoking postStop in SvdAccountsManager")
        db.close
        server.close
    }


    def receive = {
        case Init =>
            log.debug("SvdAccountsManager received Init. Running default task..")

            log.info("Spawning Coreginx")
            coreginx.start
            coreginx ! Run

            // registerFileEventFor(SvdConfig.systemHomeDir, Modified)

            //if (SvdUtils.isOSX) /* 2011-06-26 18:17:00 - dmilith - NOTE: XXX: default user definition only for OSX hosts: */
            if (!userUIDRegistered(501))
                registerUserAccount(501)

            respawnUsersActors
            self reply_? Success

        case GetAccount(uid) =>
            val account = SvdAccounts(db)(_.uid == uid).headOption
            log.trace("GetAccount(%d): %s", uid, account)
            self reply account

        case GetAccountManager(uid) =>
            log.trace("GetAccountManager(%d)", uid)
            self reply (accountManagers get uid getOrElse AccountNotFound)

        case Alive(uid) =>
            self.sender foreach (accountManagers(uid) = _)

        case GetPort =>
            self reply randomUserPort

        case x: Any =>
            log.warn("%s has received unknown signal: %s".format(this.getClass, x))

    }


    private def respawnUsersActors {
        userAccounts.foreach{
            account =>
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
