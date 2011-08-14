package com.verknowsys.served.sshd

import java.security.PublicKey
import org.apache.sshd.{SshServer => ApacheSSHServer}
// import org.apache.sshd.server.PasswordAuthenticator
import org.apache.sshd.server.PublickeyAuthenticator
import org.apache.sshd.server.session.ServerSession
// import org.apache.sshd.server.CommandFactory
// import org.apache.sshd.server.CommandFactory._
// import org.apache.sshd.server.command.UnknownCommand
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.apache.sshd.server.shell.ProcessShellFactory
import com.verknowsys.served.managers.SvdAccountsManager
import com.verknowsys.served.utils._
import com.verknowsys.served.api._
import akka.actor.{Actor, ActorRef}

class SSHD(port: Int) extends Actor with SvdExceptionHandler {
    val sshd = ApacheSSHServer.setUpDefaultServer()

    sshd.setPort(port)
    sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("hostkey.ser"))
    sshd.setPublickeyAuthenticator(new PublicKeyAuth())
    sshd.setShellFactory(new SvdShellFactory(Array("./shell"))) // XXX: hardcoded name

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

class PublicKeyAuth extends PublickeyAuthenticator with Logging {

    def authenticate(username: String, key: PublicKey, session: ServerSession) = {
        log.debug("User: %s trying to connect with key: %s", username, key)

        catchException { username.toInt } map { userUid =>
            log.trace("UserId %d", userUid)

            val res = (SvdAccountsManager !! GetAccountManager(userUid))

            log.debug("res: %s", res)

            res match {
                case Some(manager: ActorRef) =>
                    log.trace("Found manager")
                    (manager !! AuthorizeWithKey(key)) match {
                        case Some(res: Boolean) => res
                        case _ => false
                    }
                case _ =>
                    log.warn("AccountManager for userUid %d not found", userUid)
                    false
            }
        } getOrElse {
            log.warn("Username %s is not a valid userUid", username)
            false
        }
    }
}
