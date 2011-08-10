package com.verknowsys.served.systemmanager.managers

import com.verknowsys.served.SvdConfig
import com.verknowsys.served.api.git._
import com.verknowsys.served.api._
import com.verknowsys.served.db._
import com.verknowsys.served.utils._
import com.verknowsys.served.systemmanager.native._

import akka.actor.Actor.{remote, actorOf, registry}
import akka.actor.Actor
import com.verknowsys.served.systemmanager.SvdSystemManager
import com.verknowsys.served.systemmanager.SvdAccountsManager


/**
 * Account Manager - owner of all managers
 *
 * @author teamon
 */
class SvdAccountManager(val account: SvdAccount) extends SvdExceptionHandler {

    log.info("Starting AccountManager for uid: %s".format(account.uid))

    val server = new DBServer(account.dbPort, SvdConfig.userHomeDir / "%s".format(account.uid) / "%s.db".format(account.uid))
    val db = server.openClient

    val homeDir = SvdConfig.userHomeDir / account.uid.toString
    val sh = new SvdShell(account)
    val gitManager = Actor.actorOf(new SvdGitManager(account, db, homeDir / "git"))
    self startLink gitManager


    def receive = {
        case Init =>
            log.info("SvdAccountManager received Init.")

            // sh.exec("rm -rf /Users/501/THE_DB_by_initdb && initdb -D /Users/501/THE_DB_by_initdb && pg_ctl -D /Users/501/THE_DB_by_initdb start && sleep 45 && pg_ctl -D /Users/501/THE_DB_by_initdb stop")
            //             log.debug("OUTPUT: " + sh.output(0).head)
            //             sh.close(0)
            //             sh.exec("ls -lam /usr")

            val psAll = SvdLowLevelSystemAccess.processList(true)
            log.debug("All user process IDs: %s".format(psAll.mkString(", ")))
            self reply Success

            // TODO:
            // new SvdService(account, "rails app x", SvdShellOperation("rails dupa" :: "cd dupa" :: "script/rails" :: Nil)).start
            // self reply Success

        case AuthorizeWithKey(key) =>
            log.trace("Trying to find key in account: %s which have keys: %s", account, account.keys)
            self reply account.keys.find(_.key == key).isDefined

        case msg: git.Base =>
            gitManager forward msg

    }


    override def postStop {
        log.debug("Executing postStop for user svd UID: %s".format(account.uid))
        super.postStop
        sh.close
        db.close
        server.close
    }

}
