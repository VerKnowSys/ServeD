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
import org.apache.sshd._
import org.apache.sshd.server._
import org.apache.sshd.server.session._
import org.apache.sshd.server.command._
import org.apache.sshd.server.keyprovider._
import org.apache.sshd.server.shell._
import com.typesafe.config.ConfigFactory


sealed class SSHD(port: Int) extends SvdExceptionHandler {

    def this() = this(SvdConfig.sshPort)

    val sshd = SshServer.setUpDefaultServer()
    val ssm = context.actorFor("akka://%s@127.0.0.1:5555/user/SvdAccountsManager".format(SvdConfig.served))
    // val acm = context.actorFor("akka://%s@127.0.0.1:5556/remote/SvdAccountManager".format(SvdConfig.served + "remote")) // port of fixed account!

    sshd.setCommandFactory(new ScpCommandFactory(
        new CommandFactory {
            def createCommand(command: String) = new ProcessShellFactory(command.split(" ")).create
        }))
    sshd.setPort(port)
    sshd.setKeyPairProvider(new PEMGeneratorHostKeyProvider("svd-ssh-key.pem"))
    sshd.setPublickeyAuthenticator(new PublicKeyAuth)


    def receive = {
        case Init =>
            sshd.setShellFactory(new SvdShellFactory(
                Array(
                    SvdConfig.servedShell,
                    "501", // XXX: FIXME: default test user hardcoded!
                    SvdUtils.defaultShell,
                    "-i",
                    "-s"
                )
            )) //, "'%s %d'".format())))
            sshd.start
            log.info("SSHD started on port %d", port)
            sender ! Success

        case Shutdown =>
            sshd.stop


        case msg =>
            log.warn("I dont know message %s", msg)

    }

    // override def postStop {
    //     log.debug("Stopping SSHD")
    //     super.postStop
    //     sshd.stop
    // }
}


class PublicKeyAuth extends PublickeyAuthenticator with Logging {

    def authenticate(username: String, key: PublicKey, session: ServerSession): Boolean = {
        return true
        // (ssm ? GetAccountByName(username)) onSuccess {
        //     case Some(x: SvdAccount) =>
        //         log.warn("Got actor ref to remote account! Account UID is %d".format(x.uid))
        //         return true

        //         // log.warn("Checking uid: %d".format(x.uid))
        //         // (acm ? AuthorizeWithKey(key)) onSuccess {
        //         //     case x =>
        //         //         log.debug("Menager responded with: %s", x)
        //         // } onFailure {
        //         //     case x =>
        //         //         log.debug("Something bad happened? You got: %s", x)
        //         // }
        //         // ) onSuccess {
        //             // case res: Boolean =>
        //         // log.debug("RES! %s".format(res))
        //         // val shell = "%s %d".format(SvdConfig.servedShell, x.uid)
        //         // sshd.setShellFactory(new SvdShellFactory(shell.split(" ")))

        //         // log.debug("returning true")
        //                 // return res

        //         //     case x =>
        //         //         log.debug("NO KEY AUTH: %s", x)
        //         //         return false

        //         // } onFailure {
        //         //     case x =>
        //         //         log.debug("Failure with: %s", x)
        //         //         return false
        //         // }

        //     case x =>
        //         log.error("Wtf? No account? : %s", x)
        //         return false

        // } onFailure {
        //     case x =>
        //         log.warn("Failure lookup for SvdAccount: %s. reason %s", username, x)
        //         return false
        // }
        // log.warn("WTF?")
        // return false















            // convert name to uid
            // get user id
            // val userUid = 501 // XXX: hack
            // log.trace("Remote client trying to connect with UID: %s", userUid)
            // val man = context.actorFor("akka://%s/user/SvdAccountsManager".format(SvdConfig.served))
            // (man ? GetAccountManager(userUid)) onSuccess {
            //     case Some(x: SvdAccountsManager) =>
            //         context.become(dispatch)
            //         log.info("Remote client successfully connected with UID: %s", userUid)
            //         sender ! Success

            //     case None =>
            //         sender ! Error("No remote client found")

            // } onFailure {
            //     case x =>
            //         sender ! Error("Failure linking to remote client, cause of %s".format(x))
            // }

            // (ssm ? GetAccountManager(userUID)) onSuccess {
                // case Some(x: ActorRef) =>
                    // log.debug("Account manager found: %s", x)
                    // log.trace("Found manager")
    }
}
