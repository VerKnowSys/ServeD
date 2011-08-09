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
import com.verknowsys.served.utils.Logging
import com.verknowsys.served.utils.SvdExceptionHandler


import com.verknowsys.served.systemmanager._
import com.verknowsys.served.api.AuthorizeWithKey
import akka.actor.{Actor, ActorRef}

class SSHD(port: Int) extends Actor with SvdExceptionHandler {
    val sshd = ApacheSSHServer.setUpDefaultServer()

    sshd.setPort(port)
    sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("hostkey.ser"))
    sshd.setPublickeyAuthenticator(new PublicKeyAuth())
    // sshd.setShellFactory(new ProcessShellFactory(Array("/usr/local/bin/zsh", "-i", "-l")))


    log.info("Starting SSHD on port %d", port)
    sshd.start

    def receive = {
        case msg => log.warn("I dont know message %s", msg)
    }

    override def postStop = {
        super.postStop
        sshd.stop
    }
}

class PublicKeyAuth extends PublickeyAuthenticator with Logging {
    def authenticate(username: String, key: PublicKey, session: ServerSession) = {
        log.info("User: %s trying to connect with key: %s", username, key)

        // val userUid = username.toInt // XXX: Can throw exception!
        //
        // (SvdAccountsManager !! GetAccountManager(userUid)) match {
        //     case Some(manager: ActorRef) =>
        //         (manager !! AuthorizeWithKey(key)) match {
        //             case Some(res: Boolean) => res
        //             case _ => false
        //         }
        //     case _ => false
        // }
        false
    }
}
