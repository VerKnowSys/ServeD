package com.verknowsys.served.managers

import com.verknowsys.served.utils._
import com.verknowsys.served.api.git._
import com.verknowsys.served.api._
import com.verknowsys.served.git._
import com.verknowsys.served.db.DBClient

import akka.actor._
import akka.config.Supervision._
import akka.actor.Actor.{remote, actorOf, registry}


/**
 * Git Manager
 *
 * @author teamon
 */
class SvdGitManager(
        val account: SvdAccount,
        val db: DBClient,
        val gitRepositoriesLocation: String
    ) extends SvdManager with DatabaseAccess {

    log.info("Starting GitManager for account: %s in home dir: %s".format(account, gitRepositoriesLocation))


    def receive = traceReceive {
        case ListRepositories =>
            log.trace("Listing git repositories in %s", gitRepositoriesLocation)
            self reply Repositories(RepositoryDB(db).toList)

        case GetRepositoryByName(name) =>
            self reply RepositoryDB(db)(_.name == name).headOption

        case GetRepositoryByUUID(uuid) =>
            self reply RepositoryDB(db)(uuid)

        case CreateRepository(name) =>
            RepositoryDB(db)(_.name == name).headOption match {
                case Some(repo) =>
                    self reply RepositoryExistsError

                case None =>
                    log.trace("Creating new git repository: %s for account: %s".format(name, account.userName))
                    val repo = Repository(name)
                    Git.init(gitRepositoriesLocation / repo.name, bare = true)
                    db << repo
                    self reply repo
            }

        case RemoveRepository(uuid) =>
            withRepo(uuid) { repo =>
                log.trace("Removing git repository: %s for account: %s".format(repo.name, account.userName))
                db ~ repo
                SvdUtils.rmdir(gitRepositoriesLocation / repo.name + ".git")
            }

        case AddAuthorizedKey(uuid, key) =>
            withRepo(uuid) { repo =>
                db << repo.copy(authorizedKeys = repo.authorizedKeys + key)
            }

        case RemoveAuthorizedKey(uuid, key) =>
            withRepo(uuid) { repo =>
                db << repo.copy(authorizedKeys = repo.authorizedKeys - key)
            }
    }

    def withRepo(uuid: UUID)(f: (Repository) => Unit) = self reply (RepositoryDB(db)(uuid) match {
        case Some(repo) =>
            f(repo)
            Success
        case None =>
            RepositoryDoesNotExistError
    })

}
