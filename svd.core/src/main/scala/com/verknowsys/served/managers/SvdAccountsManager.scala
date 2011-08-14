package com.verknowsys.served.managers


import com.verknowsys.served.db._
import com.verknowsys.served.SvdConfig
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.events.SvdFileEvent
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.systemmanager.managers._
import com.verknowsys.served.api._
import com.verknowsys.served.api.pools._

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

object SvdAccountsManager extends GlobalActorRef(Actor.registry.actorFor[SvdAccountsManager])

class SvdAccountsManager extends SvdExceptionHandler with SvdFileEventsReactor {

    import events._

    val server = new DBServer(SvdConfig.remoteAccountServerPort, SvdConfig.systemHomeDir / SvdConfig.coreSvdAccountsDatabaseName)
    val db = server.openClient

    val rootAccount = SvdAccount("root", uid = 0)
    val svdAccountUtils = new SvdAccountUtils(db)
    import svdAccountUtils._

    log.info("SvdAccountsManager is loading")

    log.debug("User accounts registered in Account database: %s".format(SvdAccounts(db).mkString(", ")))
    log.debug("User ports registered in Account database: %s".format(SvdUserPorts(db).mkString(", ")))
    log.debug("System ports registered in Account database: %s".format(SvdSystemPorts(db).mkString(", ")))
    log.debug("User uids registered in Account database: %s".format(SvdUserUIDs(db).mkString(", ")))


    // protected val systemPasswdFilePath = SvdConfig.systemPasswdFile // NOTE: This must be copied into value to use in pattern matching

    override def postStop {
        super.postStop
        log.info("Stopping spawned user workers")
        SvdAccounts(db).foreach{
            account =>
                val pidFile = SvdConfig.userHomeDir / "%d".format(account.uid) / "%d.pid".format(account.uid)
                log.trace("PIDFile: %s".format(pidFile))
                val pid = Source.fromFile(pidFile).mkString
                log.trace("Client VM PID to be killed: %s".format(pid))
                new SvdShell(account).exec(new SvdShellOperation(
                    """
                    /bin/kill -INT %s
                    /bin/rm %s
                    """.format(pid, pidFile)
                ))
        }
        // removing also pid file of root core of svd:
        val corePid = SvdConfig.systemHomeDir / "0.pid" // XXX: hardcoded
        log.trace("Cleaning core pid file: %s with content: %s".format(corePid, Source.fromFile(corePid).mkString))
        new SvdShell(rootAccount).exec(new SvdShellOperation("/bin/rm %s".format(corePid))) // XXX: hardcoded

        log.trace("Invoking postStop in SvdAccountsManager")
        db.close
        server.close
    }


    def receive = {
        case Init =>
            log.debug("SvdAccountsManager received Init. Running default task..")
            // registerFileEventFor(SvdConfig.systemHomeDir, Modified)

            // 2011-06-26 18:17:00 - dmilith - HACK: XXX: HARDCODE: default user definition hack moved here now ;]
            // 2011-06-27 02:46:10 - dmilith - FIXME: PENDING: TODO: find out WTF in neodatis bug when more than two elements are inserted at once:
            // (1 to 10) foreach {
                // _ =>
                    if (!userUIDRegistered(501)) {
                        registerUserAccount(501)
                    }

                    if (!userUIDRegistered(10001)) {
                        registerUserAccount(10001)
                    }


                    // val ruid = randomUserUid
                    // log.trace("Adding user for %d", ruid)
                    // if (!userUIDRegistered(ruid)) {
                    //     log.trace("Added")
                    //     registerUserAccount(ruid, "żółć")
                    // }

            // }

            respawnUsersActors
            self reply_? Success

        case GetAccount(uid) =>
            val account = SvdAccounts(db)(_.uid == uid).headOption
            log.trace("GetAccount(%d): %s", uid, account)
            self reply account

        case GetAccountManager(givenUid) =>
            log.trace("GetAccountManager(%d)", givenUid)
            self reply SvdAccounts(db)(_.uid == givenUid).headOption.map { account =>
                    remote.actorFor("service:account-manager", SvdConfig.defaultHost, 12345) // XXX: hack with both port and defaultHost
            }.getOrElse(AccountNotFound)

        case GetPort =>
            self reply randomUserPort

        case x: Any =>
            log.warn("%s has received unknown signal: %s".format(this.getClass, x))

    }


    private def respawnUsersActors {
        userAccounts.foreach{
            account =>
                log.warn("Spawning account: %s".format(account))
                new SvdShell(account).exec(new SvdShellOperation("./kick " + account.uid)) // HACK
        }
    }


    /**
    * @author dmilith
    *
    *   Reads accounts from account database
    */
    protected def userAccounts = SvdAccounts(db).toList


}
