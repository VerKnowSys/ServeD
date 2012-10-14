package com.verknowsys.served.managers


import com.verknowsys.served._
import com.verknowsys.served.db._
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.events._
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.utils.signals.SvdPOSIX._
import com.verknowsys.served.systemmanager.managers._
import com.verknowsys.served.api._
import com.verknowsys.served.api.pools._
import com.verknowsys.served.services._


import scala.io.Source
import java.io._
import scala.util._


class SvdAccountUtils(db: DBClient) extends Logging with SvdUtils {
    /**
     *  @author dmilith
     *
     *   randomUserPort is a helper function to be used with SvdUserPort in API
     */
    def randomUserPort: Int = {
        val rnd = new Random(System.currentTimeMillis)
        val port = SvdPools.userPortPool.start + rnd.nextInt(SvdPools.userPortPool.end - SvdPools.userPortPool.start)
        if (portAvailable(port) && !userPortRegistered(port)) {
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
            checkOrCreateDir(userHomeDir)
            chown(userHomeDir, uid)
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
