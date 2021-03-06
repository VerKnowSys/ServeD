/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served


// import
import com.verknowsys.served.services._
import com.verknowsys.served.utils._
import com.verknowsys.served.managers.SvdAccountsManager
import com.verknowsys.served.systemmanager.SvdSystemManager
import com.verknowsys.served.sshd.SSHD
import com.verknowsys.served.api._
import scala.concurrent.duration._
import akka.actor._
import com.typesafe.config.ConfigFactory


class SvdRootBoot extends Logging with SvdActor {

    import akka.actor.OneForOneStrategy
    import akka.actor.SupervisorStrategy._


    val root = new SvdAccount(uid = 0, userName = "SuperUser")
    // val defaultServices = "Openvpn" :: "Pptpd" :: "Redis" :: "Coreginx" :: Nil // "James" :: // XXX: hardcoded system services
    // val systemServices = if (isOSX) defaultServices.drop(2) else defaultServices
    // log.trace(s"Defined system services: ${systemServices}")

    override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 25, withinTimeRange = 1 minute) {
        case _: ArithmeticException      => Resume
        case _: NullPointerException     => Restart
        case _: IllegalArgumentException => Stop
        case _: Exception                => Escalate
    }

    val system = try {
        ActorSystem(SvdConfig.served, ConfigFactory.load.getConfig(SvdConfig.served))
        } catch {
            case x: Throwable =>
                log.debug(s"Exception: $x")
                log.error("ServeD System Service must be running. No internal network accessible, hence cannot continue.")
                sys.exit(1)
        }
    // core svd actors:
    val sshd = system.actorOf(Props[SSHD].withDispatcher("svd-single-dispatcher"), "SvdSSHD") // .withDispatcher("svd-core-dispatcher")
    val ssm = system.actorOf(Props[SvdSystemManager].withDispatcher("svd-single-dispatcher"), "SvdSystemManager")
    val sam = system.actorOf(Props[SvdAccountsManager].withDispatcher("svd-single-dispatcher"), "SvdAccountsManager") //"akka://%s@deldagorin:10/user/SvdAccountsManager".format(SvdConfig.served))
    val fem = system.actorOf(Props(new SvdFileEventsManager).withDispatcher("svd-core-dispatcher"), "SvdFileEventsManager")


    // val list = (
    //     fem ::
    //     // system.actorOf(Props(new LoggingManager())) ::
    //     ssm ::
    //     sam ::
    //     // system.actorOf(Props[SvdSystemInfo].withDispatcher("svd-core-dispatcher"), "SvdSystemInfo") ::
    //     // actorOf[SvdNotificationCenter] ::
    //     sshd ::
    //     Nil) //.map(Supervise(_, Permanent))

    // supervise and autostart
    // val supervisor = Supervisor(
    //     SupervisorConfig(
    //         OneForOneStrategy(List(classOf[Exception], classOf[RuntimeException], classOf[NullPointerException]), 50, 1000),
    //         list
    //     )
    // )

    // /**
    //  *  Starts all predefined super services
    //  *
    //  * @author Daniel (dmilith) Dettlaff
    //  */
    // def startSuperServices = systemServices.map {
    //     service =>
    //         val config = new SvdServiceConfigLoader(service).config
    //         val prefix = SvdConfig.softwareRoot / config.softwareName
    //         val internalService = system.actorOf(Props(new SvdService(
    //             config,
    //             account = root,
    //             serviceRootPrefixPre = Some(prefix),
    //             servicePrefixPre = Some(SvdConfig.systemHomeDir / SvdConfig.softwareDataDir / config.name),
    //             installIndicatorPre = Some(new java.io.File(
    //                 prefix / config.softwareName.toLowerCase + "." + SvdConfig.installed))

    //         )).withDispatcher("svd-single-dispatcher"), s"SuperService-${service}")

    //         context.watch(internalService)
    //         log.info(s"Spawning SuperService: ${service}")
    // }


    // /**
    //  *  Stops all predefined super services
    //  *
    //  * @author Daniel (dmilith) Dettlaff
    //  */
    // def stopSuperServices = systemServices.map {
    //     service =>
    //         val internalService = system.actorFor(s"/user/SuperService-${service}")
    //         context.unwatch(internalService)
    //         context.stop(internalService)
    //         log.info(s"Terminating SuperService: ${service}")
    //         Thread.sleep(500)
    // }


    addShutdownHook {
        log.warn("Invoking SvdRootBoot shutdown hook")
        postStop
    }


    override def postStop = {
        log.info("Post Stopping SvdRootBoot")
        // stopSuperServices
        context.stop(sshd)
        context.stop(fem)
        context.stop(ssm)
        context.stop(sam)
        super.postStop
    }


    override def preStart = {
        super.preStart
        context.watch(fem)
        context.watch(ssm)
        context.watch(sshd)
        context.watch(sam)
        // startSuperServices

        // (sam ? Admin.RegisterAccount(SvdConfig.defaultUserName)) onSuccess {
        //     case _ =>
                // log.trace("Spawning Account Manager for each user.")
                // sam ! RegisterAccount("stefan") // XXX: hardcoded
                // sam ! RegisterAccount("waldek") // XXX: hardcoded
                // (sam ? Admin.RespawnAccounts) onSuccess {
                //     case _ =>
                //         log.info("Account Manager initialized and accounts should be spawned.")
                // } onFailure {
                //     case x =>
                //         log.error("Failure spawning accounts: %s", x)
                //         sys.exit(1)
                // }
        // }

    }


    def receive = {


        case ApiSuccess(x,y) =>
            log.debug(s"RootBoot success with msg: ${x} and content: ${y}")


        case Terminated(ref) =>
            log.debug(s"SvdAccountsManager received Terminate service for: ${ref}")
            context.unwatch(ref)


        // case Shutdown =>
        //     sshd ! Shutdown
        //     log.warn("Got shutdown. Told whole system to stop itself.")
            // Thread.sleep(SvdConfig.shutdownTimeout)
    }
}
