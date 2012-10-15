package com.verknowsys.served


import com.verknowsys.served.utils._
import com.verknowsys.served.managers.LoggingManager
import com.verknowsys.served.maintainer.SvdSystemInfo
import com.verknowsys.served.maintainer.SvdApiConnection
import com.verknowsys.served.managers.SvdAccountsManager
import com.verknowsys.served.systemmanager.SvdSystemManager
// import com.verknowsys.served.notifications.SvdNotificationCenter
import com.verknowsys.served.sshd.SSHD
import com.verknowsys.served.api._
import com.verknowsys.served.api.Admin._

import com.typesafe.config.ConfigFactory
import akka.dispatch._
import akka.pattern.ask
import akka.remote._
import akka.util.Duration
import akka.util.Timeout
import akka.util.duration._
import akka.actor._


class SvdRootBoot extends Logging with SvdExceptionHandler {


    val system = ActorSystem(SvdConfig.served, ConfigFactory.load.getConfig(SvdConfig.served))
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

    override def preStart = {
        super.preStart
        context.watch(fem)
        context.watch(ssm)
        context.watch(sshd)
        context.watch(sam)
        (sam ? RegisterAccount(SvdConfig.defaultUserName)) onSuccess {
            case _ =>
                log.trace("Spawning Account Manager for each user.")
                sam ! RegisterAccount("stefan") // XXX: hardcoded
                sam ! RegisterAccount("waldek") // XXX: hardcoded
                (sam ? RespawnAccounts) onSuccess {
                    case _ =>
                        log.info("Account Manager initialized and accounts should be spawned.")
                } onFailure {
                    case x =>
                        log.error("Failure spawning accounts: %s", x)
                        sys.exit(1)
                }
        }

    }


    def receive = {

        case Success =>
            log.debug("RootBoot success")

        case Shutdown =>
            sshd ! Shutdown
            system.shutdown
            log.warn("Got shutdown. Telling whole system to stop itself.")
            Thread.sleep(SvdConfig.shutdownTimeout)

    }
}
