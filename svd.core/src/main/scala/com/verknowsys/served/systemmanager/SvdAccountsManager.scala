package com.verknowsys.served.systemmanager


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


case class GetAccountManager(username: String)

case object SvdAccounts extends DB[SvdAccount]
case object SvdUserPorts extends DB[SvdUserPort]
case object SvdSystemPorts extends DB[SvdSystemPort]
case object SvdUserUIDs extends DB[SvdUserUID]
case object SvdUserGIDs extends DB[SvdUserGID]


class SvdAccountsManager extends Actor with SvdFileEventsReactor with SvdExceptionHandler with Logging {

    import events._

    val server = new DBServer(SvdConfig.remoteAccountServerPort, SvdConfig.systemHomeDir / SvdConfig.coreSvdAccountsDatabaseName)
    val db = server.openClient
    
    log.info("SvdAccountsManager is loading")
    
    log.debug("User accounts registered in Account database: %s".format(SvdAccounts(db).mkString(", ")))
    log.debug("User ports registered in Account database: %s".format(SvdUserPorts(db).mkString(", ")))
    log.debug("System ports registered in Account database: %s".format(SvdSystemPorts(db).mkString(", ")))
    log.debug("User uids registered in Account database: %s".format(SvdUserUIDs(db).mkString(", ")))
    log.debug("User gids registered in Account database: %s".format(SvdUserGIDs(db).mkString(", ")))

    
    // protected val systemPasswdFilePath = SvdConfig.systemPasswdFile // NOTE: This must be copied into value to use in pattern matching
    
    override def postStop {
        db.close
        server.close
    }

    
    // Safe map for fast access to AccountManagers
    protected var accountManagers: Option[Map[String, ActorRef]] = None


    def receive = {
        case Init =>
            log.debug("SvdAccountsManager received Init. Running default task..")
            // registerFileEventFor(SvdConfig.systemHomeDir, Modified)
            
            // 2011-06-26 18:17:00 - dmilith - HACK: XXX: HARDCODE: default user definition hack moved here now ;]
            if (!userUIDRegistered(501)) {
                registerUserAccount(501, "mac-user")
            }
            
            val ruid = randomUserUid
            if (!userUIDRegistered(ruid)) {
                registerUserAccount(ruid, "żabąg")
            }
            
            val ruid2 = randomUserUid
            if (!userUIDRegistered(ruid2)) {
                registerUserAccount(ruid2, "dziabąg")
            }
            
            respawnUsersActors
            self reply_? Success
        
        case GetAccountManager(username) =>
            self reply accountManagers.flatMap(_.get(username))
        
        case x: Any =>
            log.warn("%s has received unknown signal: %s".format(this.getClass, x))
            
    }

    private def respawnUsersActors {
        // 2011-06-26 18:38:42 - dmilith - PENDING: XXX: FIXME: fix respawning issues with AM
        // // kill all Account Managers
        // log.trace("Actor.registry size before: %d", registry.actors.size)
        // registry.actorsFor[SvdAccountManager] foreach { _.stop }
        // 
        // // spawn Account Manager for each account entry in passwd file
        // accountManagers = Some(userAccounts.map { account =>
        //     val manager = actorOf(new SvdAccountManager(account))
        //     self.link(manager)
        //     manager.start
        //     (account.userName, manager)
        // }.toMap)
        // log.trace("Actor.registry size after: %d", registry.actors.size)
        
        val wrapper = SvdWrapLibrary.instance
        userAccounts.foreach{
            account =>
                log.warn("Sending spawn message for account: %s".format(account))
                wrapper.sendSpawnMessage(account.uid.toString)
        }
        
        val map = Map( // 2011-06-22 16:49:20 - dmilith - XXX: FIXME: TODO: merge it into SvdAccount (gather automatically)
            "teamon" -> ("localhost", randomUserPort),
            "dmilith" -> ("localhost", randomUserPort)
        )
        
        accountManagers = Some(map.mapValues { case(host, port) =>
            val am = remote.actorFor("service:account-manager", host, port)
            log.trace("account-manager: %s :%s".format(host, port))
            // self link am
            am
        })
    }


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
     *   randomUserGid is a helper function to be used with SvdUserGID in API
     */
    def randomUserGid: Int = {
        val rnd = new Random(System.currentTimeMillis)
        val gid = SvdPools.userGidPool.start + rnd.nextInt(SvdPools.userGidPool.end - SvdPools.userGidPool.start) 
        if (!userGIDRegistered(gid)) {
            gid
        } else
            randomUserGid
    }


    /**
     *  @author dmilith
     *
     *   registers user UID with given number and name in svd database
     */
    def registerUserAccount(uid: Int, nam: String) = {
        registerUserUID(uid, nam)
        registerUserGID(uid, nam)
        db << SvdAccount(
            userName = nam,
            uid = uid,
            gid = uid
            )
    }


    /**
     *  @author dmilith
     *
     *   registers user UID with given number and name in svd database
     */
    def registerUserUID(num: Int, nam: String) =
        db << SvdUserUID(number = num, name = nam)
            
    
    /**
     *  @author dmilith
     *
     *   registers user GID with given number and name in svd database
     */
    def registerUserGID(num: Int, nam: String) =
        db << SvdUserGID(number = num, name = nam)


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


    /**
     *  @author dmilith
     *
     *   returns true if system uid is registered in svd database
     */
    def userUIDRegistered(num: Int) =
        if (SvdUserUIDs(db)(_.number == num).isEmpty)
            false
        else
            true


    /**
     *  @author dmilith
     *
     *   returns true if system gid is registered in svd database
     */
    def userGIDRegistered(num: Int) =
        if (SvdUserGIDs(db)(_.number == num).isEmpty)
            false
        else
            true

    
    /**
    * @author dmilith
    *
    *   Reads accounts from account database
    */
    protected def userAccounts = SvdAccounts(db).toList
     
     
}
