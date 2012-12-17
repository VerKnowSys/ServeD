package com.verknowsys.served.managers


import com.verknowsys.served._
import com.verknowsys.served.db._
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.Events._
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.utils.signals.SvdPOSIX._
import com.verknowsys.served.api.{SvdUserPort, SvdUserUID, SvdAccount, SvdSystemPort, SvdUserDomain}
import com.verknowsys.served.api.pools._
import com.verknowsys.served.services._

import scala.io.Source
import java.io._
import scala.util._


object SvdAccountUtils extends SvdUtils {

    /**
     *  @author dmilith
     *
     *   randomUserPort is a helper function to be used with SvdUserPort in API
     */
    def randomFreePort: Int = {
        val rnd = new Random(System.currentTimeMillis)
        val port = SvdPools.userPortPool.start + rnd.nextInt(SvdPools.userPortPool.end - SvdPools.userPortPool.start)
        if (portAvailable(port)) {
            port
        } else
            randomFreePort
    }

}


class SvdAccountUtils(db: DBClient) extends SvdAkkaSupport with Logging {

    import SvdAccountUtils._


    def randomUserPort: Int = {
        val port = randomFreePort
        if (!userPortRegistered(port)) {
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
        if (portAvailable(port) && !systemPortRegistered(port)) {
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
     *   registers user with given name and uid number in svd database
     */
    def registerUserAccount(name: String, uid: Int): Unit = {
        val userManagerPort = randomUserPort
        val userHomeDir = SvdConfig.userHomeDir / "%d".format(uid)

        def performChecks(managerPort: Int = userManagerPort) {
            log.trace("Performing user registration checks and making missing directories")
            checkOrCreateDir(userHomeDir)
            // ("Backup" :: "SoftwareData" :: "WebApps" :: Nil).map { // XXX: HARDCODE
            //     sub =>
            //         log.trace("Chowning %s", userHomeDir / sub)
            //         chown(userHomeDir / sub, uid)
            // }
            log.debug("Creating Akka configuration…")
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
            log.debug("Account Registered Successfully.")
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


    /**
     * @author Daniel (dmilith) Dettlaff
     *
     *  Checks if given domain is already registered for user
     *
     */
    def userDomainRegistered(domain: String) = {
        if (SvdUserDomains(db)(_.name == domain).isEmpty)
            false
        else
            true
    }


    /**
     * @author Daniel (dmilith) Dettlaff
     *
     *  Registers user domain
     *
     */
    def registerUserDomain(domain: String) = {
        if (!userDomainRegistered(domain)) {
            db << SvdUserDomain(name = domain)
        } else {
            log.trace("Domain already registered: %s", domain)
        }
    }


}