package com.verknowsys.served.sshd

import com.verknowsys.served.sshd._
import com.verknowsys.served.managers.SvdAccountsManager
import com.verknowsys.served.utils._
import com.verknowsys.served.SvdConfig
import com.verknowsys.served.api._
import akka.actor._
import akka.dispatch._
import akka.pattern.ask
import akka.remote._
import akka.util.Duration
import akka.util.Timeout
import akka.util.duration._


import java.security.PublicKey
import org.apache.sshd.{SshServer => ApacheSSHServer}
import org.apache.sshd.server.PublickeyAuthenticator
import org.apache.sshd.server.session.ServerSession
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.apache.sshd.server.shell.ProcessShellFactory
import com.typesafe.config.ConfigFactory
// import org.apache.sshd.server.PasswordAuthenticator
// import org.apache.sshd.server.CommandFactory
// import org.apache.sshd.server.CommandFactory._
// import org.apache.sshd.server.command.UnknownCommand


class SSHD(port: Int) extends Actor with SvdExceptionHandler {

    // implicit val timeout = Timeout(30 seconds)

    val sshd = ApacheSSHServer.setUpDefaultServer()
    sshd.setPort(port)
    sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("hostkey.ser"))
    val ssm = context.actorFor("akka://%s@127.0.0.1:5555/user/SvdAccountsManager".format(SvdConfig.served))
    sshd.setPublickeyAuthenticator(new PublicKeyAuth(ssm))
    sshd.setShellFactory(new SvdShellFactory(Array(SvdConfig.servedShell)))

    override def preStart {
        log.info("Starting SSHD on port %d", port)
        sshd.start
    }


    def receive = {
        case msg => log.warn("I dont know message %s", msg)
    }

    override def postStop {
        log.debug("Stopping SSHD")
        super.postStop
        sshd.stop
    }
}

class PublicKeyAuth(ssm: ActorRef) extends PublickeyAuthenticator with Logging {



    def authenticate(username: String, key: PublicKey, session: ServerSession): Boolean = {
        log.debug("User with name alias: %s is trying to connect with key: %s", username, key)

        catchException { username } map { userName =>
            log.debug("User name %s".format(userName))

            // convert name to uid
            // get user id
            // val userUid = 501 // XXX: hack

            (ssm ? GetAccountByName(userName)) onSuccess {
                case Some(x: SvdAccount) =>
                    log.debug("Got actor ref to remote account!")
                    log.debug("Checking name: %s vs %s".format(userName, x.userName))
                    return true
                case None =>
                    log.error("Wtf? No account")
                    false

            } onFailure {
                case x =>
                    log.warn("Failure lookup for SvdAccount: %s. reason %s", userName, x)
                    false
            }
            false

            // (ssm ? GetAccountManager(userUid)) onSuccess {
            //     case Some(x: ActorRef) =>
            //         log.debug("Account found: %s", x)
            //         log.trace("Found manager")
            //         (x ? AuthorizeWithKey(key)) onSuccess {
            //             case res: Boolean =>
            //                 log.debug("RES! %s".format(res))
            //                 return res
            //             case _ =>
            //                 log.debug("NO KEY AUTH HACK: RES! %s")
            //                 return true
            //                 // false
            //         }
            //     case _ =>
            //         log.debug("Account NOT found: %s", userName)
            //         false

            // } onFailure {
            //     case _ =>
            //         log.error("Failed connection to get account manager")
            // }
            // false
            // val res = Await.result(future, timeout.duration)

            // log.debug("res: %s", res)

            //     case _ =>
            //         log.warn("AccountManager for userUid %d not found", userUid)
            //         false
            // }

        } getOrElse {
            log.warn("Username %s is not a valid userUid", username)
            false
        }
    }
}
