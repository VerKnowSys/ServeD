package com.verknowsys.served.managers

import com.verknowsys.served.utils._
import com.verknowsys.served.api.git._
import com.verknowsys.served.api._
import com.verknowsys.served.git._
import com.verknowsys.served.db.DBClient

import akka.actor._


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
        log.debug("Initializing repositories of %s in: %s".format(gitRepositoriesLocation, Repositories(RepositoryDB(db).toList)))
    }


    def receive = traceReceive {

        case ListRepositories =>
            log.trace("Listing git repositories in %s", gitRepositoriesLocation)
            sender ! Repositories(RepositoryDB(db).toList)

        case GetRepositoryByName(name) =>
            sender ! RepositoryDB(db)(_.name == name).headOption

        case GetRepositoryByUUID(uuid) =>
            sender ! RepositoryDB(db)(uuid)

        case CreateRepository(name) =>
            RepositoryDB(db)(_.name == name).headOption match {
                case Some(repo) =>
                    sender ! RepositoryExistsError

                case None =>
                    log.trace("Creating new git repository: %s for account: %s".format(name, account.userName))
                    val repo = Repository(name)
                    Git.init(gitRepositoriesLocation / repo.name, bare = true)
                    db << repo
                    sender ! repo
            }

        case RemoveRepository(uuid) =>
            withRepo(uuid) { repo =>
                log.trace("Removing git repository: %s for account: %s".format(repo.name, account.userName))
                val repoLocation = gitRepositoriesLocation / repo.name + ".git"
                if (!new java.io.File(repoLocation).exists) {
                    sender ! RepositoryDoesNotExistError
                } else {
                    log.trace("Removing repository: %s".format(repoLocation))
                    db ~ repo
                    rm_r(repoLocation)
                    sender ! Success
                }
            }

        case AddAuthorizedKey(uuid, key) =>
            withRepo(uuid) { repo =>
                db << repo.copy(authorizedKeys = repo.authorizedKeys + key)
            }
            sender ! Success

        case RemoveAuthorizedKey(uuid, key) =>
            withRepo(uuid) { repo =>
                db << repo.copy(authorizedKeys = repo.authorizedKeys - key)
            }
            sender ! Success

        case Repository(name, authorizedKeys, uuid) =>
            log.debug("Got new repository named: %s with keys: %s", name, authorizedKeys)

        case RepositoryExistsError =>
            log.warn("Repository already exists!")

        case Shutdown =>
            log.debug("Shutting down Git Manager")
            context.stop(self)

    }

    def withRepo(uuid: UUID)(f: (Repository) => Unit) = sender ! (RepositoryDB(db)(uuid) match {
        case Some(repo) =>
            f(repo)
            Success
        case None =>
            RepositoryDoesNotExistError
    })

}
