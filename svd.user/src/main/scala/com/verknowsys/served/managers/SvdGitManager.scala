package com.verknowsys.served.managers


import com.verknowsys.served.api._
import com.verknowsys.served.git._
import com.verknowsys.served.api.git._
import com.verknowsys.served.db._
import com.verknowsys.served.utils._

import akka.actor._


object RepositoryDB extends DB[Repository]


/**
 * Git Manager
 *
 * @author teamon
 * @author dmilith
 */
class SvdGitManager(
        val account: SvdAccount,
        val db: DBClient,
        val gitRepositoriesLocation: String
    ) extends SvdManager with DatabaseAccess {


    override def preStart = {
        super.preStart
        log.info("Starting GitManager for account: %s in home dir: %s".format(account, gitRepositoriesLocation))
        log.debug("Checking existance of %s", gitRepositoriesLocation)
        checkOrCreateDir(gitRepositoriesLocation)
        log.debug("Initializing repositories of %s in: %s".format(gitRepositoriesLocation, Git.Repositories(RepositoryDB(db).toList)))
    }


    override def preRestart(cause: Throwable, message: Option[Any]) = {
        log.debug("preRestart of account: %s caused by: %s with message: %s", account, cause, message)
        super.preRestart(cause, message)
    }


    override def postStop = {
        db.close
        log.debug("postStop of account: %s", account)
        super.postStop
    }


    def receive = traceReceive {

        case Git.ListRepositories =>
            log.trace("Listing git repositories in %s", gitRepositoriesLocation)
            sender ! Git.Repositories(RepositoryDB(db).toList)

        case Git.GetRepositoryByName(name) =>
            sender ! RepositoryDB(db)(_.name == name).headOption

        case Git.GetRepositoryByUUID(uuid) =>
            sender ! RepositoryDB(db)(uuid)

        case Git.CreateRepository(name) =>
            RepositoryDB(db)(_.name == name).headOption match {
                case Some(repo) =>
                    sender ! Git.RepositoryExistsError

                case None =>
                    log.trace("Creating new git repository: %s for account: %s".format(name, account.userName))
                    val repo = Repository(name)
                    GitCore.init(gitRepositoriesLocation / repo.name, bare = true)
                    db << repo
                    sender ! repo
            }

        case Git.RemoveRepository(uuid) =>
            withRepo(uuid) { repo =>
                log.trace("Removing git repository: %s for account: %s".format(repo.name, account.userName))
                val repoLocation = gitRepositoriesLocation / repo.name + ".git"
                if (!new java.io.File(repoLocation).exists) {
                    sender ! Git.RepositoryDoesNotExistError
                } else {
                    log.trace("Removing repository: %s".format(repoLocation))
                    db ~ repo
                    rm_r(repoLocation)
                    sender ! Success
                }
            }

        case Git.AddAuthorizedKey(uuid, key) =>
            withRepo(uuid) { repo =>
                db << repo.copy(authorizedKeys = repo.authorizedKeys + key)
            }
            sender ! Success

        case Git.RemoveAuthorizedKey(uuid, key) =>
            withRepo(uuid) { repo =>
                db << repo.copy(authorizedKeys = repo.authorizedKeys - key)
            }
            sender ! Success

        case Repository(name, authorizedKeys, uuid) =>
            log.debug("Got new repository named: %s with keys: %s", name, authorizedKeys)

        case Git.RepositoryExistsError =>
            log.warn("Repository already exists!")

    }


    def withRepo(uuid: java.util.UUID)(f: (Repository) => Unit) = sender ! (RepositoryDB(db)(uuid) match {
        case Some(repo) =>
            f(repo)
            Success
        case None =>
            Git.RepositoryDoesNotExistError
    })

}
