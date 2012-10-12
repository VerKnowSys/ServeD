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
    val fem = system.actorOf(Props[SvdFileEventsManager].withDispatcher("svd-core-dispatcher"), "SvdFileEventsManager")

    val list = (
        fem ::
        // system.actorOf(Props(new LoggingManager())) ::
        ssm ::
        sam ::
        // system.actorOf(Props[SvdSystemInfo].withDispatcher("svd-core-dispatcher"), "SvdSystemInfo") ::
        // actorOf[SvdNotificationCenter] ::
        sshd ::
        Nil) //.map(Supervise(_, Permanent))

    // supervise and autostart
    // val supervisor = Supervisor(
    //     SupervisorConfig(
    //         OneForOneStrategy(List(classOf[Exception], classOf[RuntimeException], classOf[NullPointerException]), 50, 1000),
    //         list
    //     )
    // )


    def receive = {
        case Init =>
            (fem ? Init) onSuccess {
                case _ =>
                    log.info("File Events Manager initialized")
                    context.watch(fem)
                    (sam ? Init) onSuccess {
                        case _ =>
                            def spawnSAM {
                                (sam ? RegisterAccount(SvdConfig.defaultUserName)) onSuccess {
                                    case _ =>
                                        log.trace("Spawning Account Manager for each user.")
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
                            (sshd ? Init) onSuccess { // spawn sshd after user accounts were started
                                case _ =>
                                    log.info("SSHD initialized")
                                    context.watch(sshd)
                                    spawnSAM

                            } onFailure { // spawn accounts no matter if SSHD failed or not
                                case x =>
                                    log.error("SSHD init failure: %s", x)
                                    spawnSAM
                            }
                            context.watch(sam)
                            (ssm ? Init) onSuccess {
                                case _ =>
                                    log.info("System Manager initialized")
                                    context.watch(ssm)
                            } onFailure {
                                case x =>
                                    log.error("Failure spawning SystemManager: %s", x)
                                    sys.exit(1)
                            }
                    } onFailure {
                        case x =>
                            log.error("Failure spawning AccountsManager: %s", x)
                            sys.exit(1)
                    }
            } onFailure {
                case x =>
                    log.error("Failure spawning File Events Manager: %s", x)
                    sys.exit(1)
            }


        case Success =>
            log.debug("RootBoot success")

        case Shutdown =>
            sshd ! Shutdown
            system.shutdown
            log.debug("Got shutdown")

    }
}
