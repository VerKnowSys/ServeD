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

    val systemServices = "Redis" :: Nil // XXX: hardcoded system services


    override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 25, withinTimeRange = 1 minute) {
        case _: ArithmeticException      => Resume
        case _: NullPointerException     => Restart
        case _: IllegalArgumentException => Stop
        case _: Exception                => Escalate
    }

    val system = ActorSystem(SvdConfig.served, ConfigFactory.load.getConfig(SvdConfig.served))
    // core svd actors:
    val sshd = system.actorOf(Props[SSHD].withDispatcher("svd-single-dispatcher"), "SvdSSHD") // .withDispatcher("svd-core-dispatcher")
    val ssm = system.actorOf(Props[SvdSystemManager].withDispatcher("svd-single-dispatcher"), "SvdSystemManager")
    val sam = system.actorOf(Props[SvdAccountsManager].withDispatcher("svd-single-dispatcher"), "SvdAccountsManager") //"akka://%s@deldagorin:10/user/SvdAccountsManager".format(SvdConfig.served))
    val fem = system.actorOf(Props(new SvdFileEventsManager).withDispatcher("svd-core-dispatcher"), "SvdFileEventsManager")

    val internalRedis = system.actorOf(Props(new SvdSuperService("Redis")).withDispatcher("svd-single-dispatcher"), "SuperService-Redis")

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


    override def postStop = {
        context.stop(sshd)
        context.stop(fem)
        context.stop(ssm)
        context.stop(internalRedis)
        context.stop(sam)
    }


    override def preStart = {
        super.preStart
        context.watch(fem)
        context.watch(ssm)
        context.watch(sshd)
        context.watch(sam)
        context.watch(internalRedis)

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

        case ApiSuccess =>
            log.debug("RootBoot success")

        case Terminated(ref) =>
            log.debug(s"SvdAccountsManager received Terminate service for: ${ref}")
            context.unwatch(ref)


        // case Shutdown =>
        //     sshd ! Shutdown
        //     log.warn("Got shutdown. Told whole system to stop itself.")
            // Thread.sleep(SvdConfig.shutdownTimeout)
    }
}
