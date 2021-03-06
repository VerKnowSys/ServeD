/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.sshd

import com.verknowsys.served.utils._
import com.verknowsys.served.SvdConfig
import com.verknowsys.served.api._
import akka.actor._

import java.security.PublicKey
import org.apache.sshd._
import org.apache.sshd.{SshServer => ApacheSSHServer}
import org.apache.sshd.server._
import org.apache.sshd.server.session._
import org.apache.sshd.server.command._
import org.apache.sshd.server.keyprovider._
import org.apache.sshd.server.shell._


sealed class SSHD(port: Int) extends SvdActor {

    def this() = this(SvdConfig.sshPort)

    val sshd = SshServer.setUpDefaultServer()
    val ssm = context.actorFor("akka://%s@%s:%d/user/SvdAccountsManager".format(SvdConfig.served, SvdConfig.remoteApiServerHost, SvdConfig.remoteApiServerPort))


    override def preStart = {
        super.preStart
        sshd.setCommandFactory(new ScpCommandFactory(
            new CommandFactory {
                def createCommand(command: String) = new ProcessShellFactory(command.split(" ")).create
            }
        ))
        sshd.setPort(port)
        sshd.setKeyPairProvider(new PEMGeneratorHostKeyProvider("svd-ssh-key.pem"))

        log.info("SSHD prepared on port %d but not listening yet", port)
    }


    def started(listOfKeys: Set[AccessKey], account: SvdAccount): Receive = {

        case Shutdown =>
            context.unbecome
            sshd.stop

        case _ =>
            sender ! Taken(account.uid)

    }


    def receive = {

        case InitSSHChannelForUID(forUID: Int) =>
            // log.debug("Got shell base for uid: %d", forUID)
            // (sender ? ListKeys) onSuccess {
            //     case set: Set[_] =>
            //         log.trace("Listing user SSH keys: %s", set)

            //         (ssm ? GetAccount(forUID)) onSuccess {
            //             case Some(account: SvdAccount) =>
            //                 val list = set.asInstanceOf[Set[AccessKey]]
            //                 log.debug("Become available for uid %d", account.uid)
            //                 context.become(started(list, account))
            //                 sshd.setPublickeyAuthenticator(new PublicKeyAuth(list, account))
            //                 sshd.setShellFactory(new SvdShellFactory(
            //                     Array(
            //                         SvdConfig.servedShell,
            //                         "%d".format(account.uid),
            //                         defaultShell,
            //                         "-i",
            //                         "-s"
            //                     )
            //                 ))
            //                 sshd.start

            //             case x =>
            //                 sender ! ApiError("We don't like this: %s".format(x))

            //         } onFailure {
            //             case x =>
            //                 sender ! ApiError("I'm trying, but, come on. Wtf?: %s".format(x))
            //         }

            //     case x =>
            //         sender ! ApiError("Got something weird for uid %d - %s".format(forUID, x))

            // } onFailure {
            //     case x =>
            //         sender ! ApiError("Failed to ask for user SSHD keys with error: %s".format(x))
            // }


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
