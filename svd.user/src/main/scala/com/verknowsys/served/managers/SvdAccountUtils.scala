/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.managers


import com.verknowsys.served._
import com.verknowsys.served.db._
import com.verknowsys.served.utils._
import com.verknowsys.served.api.{SvdUserPort, SvdUserUID, SvdAccount, SvdSystemPort, SvdUserDomain}
import com.verknowsys.served.api.pools._

import scala.util._
import java.io.File


object SvdAccountUtils extends SvdUtils {

    /**
     *   randomUserPort is a helper function to be used with SvdUserPort in API
     *
     *  @author dmilith
     */
    def randomFreePort: Int = {
        val rnd = new Random(System.currentTimeMillis)
        val port = SvdPools.userPortPool.start + rnd.nextInt(SvdPools.userPortPool.end - SvdPools.userPortPool.start)
        if (portAvailable(port)) {
            port
        } else
            randomFreePort
    }


    // /**
    //  *  Load autostart marks from services software data dir.
    //  *
    //  *  @author dmilith
    //  */
    // def loadServicesList(servicesLocationDir: String) = {
    //     val res = listDirectories(servicesLocationDir)
    //     log.trace(s"Services with softwareData list: ${res.mkString(",")}")
    //     res.map {
    //         dir =>
    //             if (new File(dir.toString / SvdConfig.serviceAutostartFile).exists) {
    //                 log.debug(s"Found autostart for ${dir}")
    //                 dir.toString.split("/").last
    //             } else {
    //                 log.debug(s"No autostart for ${dir}")
    //                 ""
    //             }
    //     }.filterNot(_.isEmpty)
    // }


    // /**
    //  *  Load services list by pid files (they should be running).
    //  *
    //  *  @author dmilith
    //  */
    // def loadRunningServicesList(servicesLocationDir: String) = {
    //     val res = listDirectories(servicesLocationDir)
    //     res.map {
    //         dir =>
    //             if ((new File(dir.toString / SvdConfig.defaultServicePidFile).exists) ||
    //                 (new File(dir.toString / "database" / "postmaster.pid").exists) // XXX: hack
    //                 ) { // XXX: hardcode
    //                 log.debug(s"Found pid for ${dir}")
    //                 dir.toString.split("/").last
    //             } else {
    //                 log.debug(s"No pid for ${dir}")
    //                 ""
    //             }
    //     }.filterNot(_.isEmpty)
    // }


    // /**
    //  *  Cleans autostart mark from services software data dir.
    //  *
    //  *  @author dmilith
    //  */
    // def cleanServicesAutostart(servicesLocationDir: String) {
    //     val res = listDirectories(servicesLocationDir)
    //     log.warn(s"Services to autostart: ${res.mkString(",")}")
    //     res.map {
    //         dir =>
    //             val file = new java.io.File(dir.toString / SvdConfig.serviceAutostartFile)
    //             if (file.exists) {
    //                 log.debug("Removing autostart file: %s", file)
    //                 rm_r(file.toString)
    //             }
    //     }
    // }



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
     *   randomSystemPort is a helper function to be used with SvdSystemPort in API
     *
     *  @author dmilith
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
     *   randomUserUid is a helper function to be used with SvdUserUID in API
     *
     *  @author dmilith
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
     *   registers user with given name and uid number in svd database
     *
     *  @author dmilith
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
            log.trace(s"Generated user manager port: ${userManagerPort} for account with uid: ${uid}")
            if (!userPortRegistered(userManagerPort)) {
                registerUserPort(userManagerPort)
                log.trace(s"Registered user manager port: ${userManagerPort}")
            } else {
                log.trace(s"Registering once again cause of port dup: ${userManagerPort}")
                registerUserAccount(name, uid)
            }
            registerUserUID(uid)
            performChecks()
            log.debug(s"Writing account data of uid: ${uid}")
            db << SvdAccount(userName = name, uid = uid, accountManagerPort = userManagerPort)
            log.debug("Account Registered Successfully.")
        } else {
            val userAccount = SvdAccounts(db).filter{_.uid == uid}.head
            val userManagerPort = userAccount.accountManagerPort
            log.trace(s"User already registered with manager port: ${userManagerPort}, but still validating existance of akka user file and home directory: ${userHomeDir}")
            performChecks(userManagerPort)
        }
    }


    /**
     *   registers user UID with given number and name in svd database
     *
     *  @author dmilith
     */
    def registerUserUID(num: Int) =
        db << SvdUserUID(number = num)


    /**
     *   registers user port with given number in svd database
     *
     *  @author dmilith
     */
    def registerUserPort(num: Int) =
        db << SvdUserPort(number = num)


    /**
     *   registers system port with given number in svd database
     *
     *  @author dmilith
     */
    def registerSystemPort(num: Int) =
        db << SvdSystemPort(number = num)


    /**
     *   returns true if user port is registered in svd database
     *
     *  @author dmilith
     */
    def userPortRegistered(num: Int) =
        if (SvdUserPorts(db)(_.number == num).isEmpty)
            false
        else
            true


    /**
     *   returns true if system port is registered in svd database
     *
     *  @author dmilith
     */
    def systemPortRegistered(num: Int) =
        if (SvdSystemPorts(db)(_.number == num).isEmpty)
            false
        else
            true


    /**s
     *   returns true if system uid is registered in svd database
     *
     *  @author dmilith
     */
    def userUIDRegistered(num: Int) =
        if (SvdUserUIDs(db)(_.number == num).isEmpty)
            false
        else
            true


    /**
     *  Checks if given domain is already registered for user
     *
     * @author Daniel (dmilith) Dettlaff
     */
    def userDomainRegistered(domain: String) = {
        if (SvdUserDomains(db)(_.name == domain).isEmpty)
            false
        else
            true
    }


    /**
     *  Registers user domain
     *
     * @author Daniel (dmilith) Dettlaff
     */
    def registerUserDomain(domain: String) = {
        if (!userDomainRegistered(domain)) {
            db << SvdUserDomain(name = domain)
        } else {
            log.trace(s"Domain already registered: ${domain}")
        }
    }


}
