/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served


import com.verknowsys.served._
import com.verknowsys.served.managers._
import com.verknowsys.served.utils._
import com.verknowsys.served.api._
import akka.util.duration._
import akka.util.Timeout
import akka.actor._
import akka.pattern.ask
import com.typesafe.config.ConfigFactory
import scala.io._
import java.io.File
import java.lang.{System => JSystem}


class SvdUserBoot(userUID: Int) extends Logging with SvdActor with SvdAkkaSupport {

    import akka.actor.OneForOneStrategy
    import akka.actor.SupervisorStrategy._


    implicit val timeout = Timeout(SvdConfig.headlessTimeout / 1000 seconds) // cause of standard
    override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 25, withinTimeRange = 1 minute) {
        case _: ArithmeticException      => Resume
        case _: NullPointerException     => Restart
        case _: IllegalArgumentException => Stop
        case _: Exception                => Escalate
    }

    val configFile = SvdConfig.userHomeDir / "%d".format(userUID) / SvdConfig.defaultAkkaConfName

    if (!new File(configFile).exists) {
        log.info("Spawning headless for uid: %d".format(userUID))
        createAkkaUserConfIfNotExistant(userUID, userUID + 1026)
    }

    val akkaConfigContent = Source.fromFile(configFile).getLines.mkString("\n")
    log.trace("Read akka configuration for account: %s", akkaConfigContent)
    val system = try {
        ActorSystem(SvdConfig.served, ConfigFactory.parseString(akkaConfigContent).getConfig("ServeDremote"))
    } catch {
        case _ =>
            ActorSystem(SvdConfig.served, ConfigFactory.parseString(akkaConfigContent).getConfig("ServeDheadless"))
    }
    val accountsManager = system.actorFor("akka://%s@%s:%d/user/SvdAccountsManager".format(SvdConfig.served, SvdConfig.remoteApiServerHost, SvdConfig.remoteApiServerPort))


    (accountsManager ? System.GetPort) onSuccess {

        case anyPort: Int =>
            val bootAccount = SvdAccount(uid = userUID, userName = "a boot user %d".format(userUID))
            val am = system.actorOf(Props(new SvdAccountManager(bootAccount, self)).withDispatcher("svd-single-dispatcher"), "SvdAccountManager") // NOTE: actor name is significant for remote actors!!
            log.info("UserBoot connected to remote SvdRoot at: %s for UID: %d", SvdConfig.remoteApiServerHost, userUID)

    } onFailure {

        case x =>
            // launching headless mode
            log.info("Launching svduser headless mode for UID: %d".format(userUID))
            val bootAccount = SvdAccount(uid = userUID, userName = "a headless user %s".format(userUID))
            val am = system.actorOf(Props(new SvdAccountManager(bootAccount, self, headless = true)).withDispatcher("svd-single-dispatcher"), "SvdAccountManager")
            log.info("UserBoot spawned for UID: %d", userUID)

    }


    addShutdownHook {
        log.warn("userboot Shutdown Hook invoked")
        Thread.sleep(SvdConfig.shutdownTimeout)
        system.shutdown // shutting down main account actor manager
    }


    override def preStart = {
        log.debug("Prestart in SvdUserBoot")
        super.preStart
    }


    def receive = {


        case Success =>
            log.debug("UserBoot success.")


        case Maintenance.RestartAccountManager =>
            log.debug("Killing Account Manager on demand of user: %s.", userUID)
            sender ! Success
            system.shutdown
            sys.exit(0)


        case Terminated(ref) =>
            log.debug("SvdUserBoot Terminated service actor: %s".format(ref))
            context.unwatch(ref)


    }

}
