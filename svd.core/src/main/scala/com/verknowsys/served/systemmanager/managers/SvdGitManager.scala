package com.verknowsys.served.systemmanager.managers

import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.systemmanager._
import com.verknowsys.served.utils._
import com.verknowsys.served.api.git._
import com.verknowsys.served.api.Success
import com.verknowsys.served.git._
import com.verknowsys.served.db.DBClient
import com.verknowsys.served.managers.DatabaseAccess

import akka.actor._
import akka.config.Supervision._
import akka.actor.Actor.{remote, actorOf, registry}

/**
 * Git Manager
 * 
 * @author teamon
 */
class SvdGitManager(val account: SvdAccount, val db: DBClient) extends SvdManager(account) with DatabaseAccess {
    log.info("Starting GitManager for account: " + account)


    def receive = {
        case ListRepositories =>
            log.trace("Listing git repositories in %s", gitHomeDir)
            val repos = RepositoryDB(db).toList
            log.trace(repos.toString)
            self reply Repositories(repos)
            // self reply Repositories(Nil)

        case CreateRepository(name) =>
            RepositoryDB(db)(_.name == name).headOption match {
                case Some(repo) =>
                    self reply RepositoryExistsError
                case None =>
                    log.trace("Creating new git repository: %s for account: %s".format(name, account.userName))
                    val repo = Repo(name)
                    Git.init(gitHomeDir / repo.name, bare = true)
                    db << repo
                    self reply repo
            }

        case RemoveRepository(uuid) =>
            RepositoryDB(db)(uuid) match {
                case Some(repo) =>
                    log.trace("Removing git repository: %s for account: %s".format(repo.name, account.userName))
                    db ~ repo
                    SvdUtils.rmdir(gitHomeDir / repo.name + ".git")
                    self reply Success
                case None =>
                    self reply RepositoryDoesNotExistError
            }
    }

    
    protected lazy val gitHomeDir = SvdUtils.checkOrCreateDir(account.homeDir / "git")
}
