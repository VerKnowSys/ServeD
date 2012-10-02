package com.verknowsys.served.sshd

import com.verknowsys.served.sshd._
import com.verknowsys.served.managers.SvdAccountsManager
import com.verknowsys.served.utils._
import com.verknowsys.served.SvdConfig
import com.verknowsys.served.api._
import com.verknowsys.served.api.accountkeys._
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
    val ssm = context.actorFor("akka://%s@127.0.0.1:%d/user/SvdAccountsManager".format(SvdConfig.served, SvdConfig.remoteApiServerPort))
    // val acm = context.actorFor("akka://%s@127.0.0.1:5556/remote/SvdAccountManager".format(SvdConfig.served + "remote")) // port of fixed account!


    def started(listOfKeys: Set[AccessKey], account: SvdAccount): Receive = {

        case Init =>

            log.debug("SSHD Becoming available for uid %d", account.uid)
            sshd.setPublickeyAuthenticator(new PublicKeyAuth(listOfKeys, account))
            sshd.setShellFactory(new SvdShellFactory(
                Array(
                    SvdConfig.servedShell,
                    "%d".format(account.uid),
                    SvdUtils.defaultShell,
                    "-i",
                    "-s"
                )
            ))
            sshd.start

        case Shutdown =>
            context.unbecome
            sshd.stop

        case _ =>
            sender ! Taken(account.uid)

    }


    def receive = {

        case Init =>
            sshd.setCommandFactory(new ScpCommandFactory(
                new CommandFactory {
                    def createCommand(command: String) = new ProcessShellFactory(command.split(" ")).create
                }
            ))
            sshd.setPort(port)
            sshd.setKeyPairProvider(new PEMGeneratorHostKeyProvider("svd-ssh-key.pem"))

            log.info("SSHD prepared on port %d but not listening yet", port)
            sender ! Success


        case InitSSHChannelForUID(forUID: Int) =>
            log.debug("SSHD Got shell base for uid: %d", forUID)
            (sender ? ListKeys) onSuccess {

                case set: Set[_] =>
                    log.debug("Listing user SSH keys: %s", set)

                    (ssm ? GetAccount(forUID)) onSuccess {
                        case Some(account: SvdAccount) =>
                            context.become(started(set.asInstanceOf[Set[AccessKey]], account))
                            self ! Init // hit message after it became listening state

                        case x =>
                            log.debug("We don't like this: %s", x)

                    } onFailure {
                        case x =>
                            log.debug("I'm trying, but, come on. Wtf?: %s", x)
                    }

                case x =>
                    log.debug("Got something weird for uid %d - %s", forUID, x)


            } onFailure {
                case x =>
                    sender ! Error("Failed to ask for user SSHD keys")
            }


        case Shutdown =>
            log.debug("Shutdown requested. Bye")
            context.stop(self)


        case msg =>
            log.warn("I dont know message %s", msg)

    }

    // override def postStop {
    //     log.debug("Stopping SSHD")
    //     super.postStop
    //     sshd.stop
    // }
}


class PublicKeyAuth(setOfAccessKeys: Set[AccessKey], account: SvdAccount) extends PublickeyAuthenticator with Logging {


    def authenticate(username: String, key: PublicKey, session: ServerSession) = {
        log.debug("Pending SSH Authentication username: %s, key: %s, session: %s", username, key, session)

        val set = setOfAccessKeys.filter{
            _.key == key
        }
        if (set.isEmpty) {
            log.debug("Set empty. No matching keys")
            false
        } else {
            log.debug("Set not empty. Matching key found. Checking user name: %s", username == account.userName)

            username == account.userName
        }
    }

}
