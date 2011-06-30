package com.verknowsys.served.systemmanager.managers

import com.verknowsys.served._
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.systemmanager._
import com.verknowsys.served.utils._
import com.verknowsys.served.api.git._
import com.verknowsys.served.api._
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
class SvdGitManager(
        val account: SvdAccount,
        val db: DBClient,
        val accountHomeDir: String
    ) extends SvdManager(account) with DatabaseAccess {
        
    log.info("Starting GitManager for account: %s in home dir: %s".format(account, accountHomeDir))


    def receive = {
        case ListRepositories =>
            log.trace("Listing git repositories in %s", accountHomeDir)
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
                    Git.init(accountHomeDir / repo.name, bare = true)
                    db << repo
                    self reply repo
            }

        case RemoveRepository(uuid) =>
            RepositoryDB(db)(uuid) match {
                case Some(repo) =>
                    log.trace("Removing git repository: %s for account: %s".format(repo.name, account.userName))
                    db ~ repo
                    SvdUtils.rmdir(accountHomeDir / repo.name + ".git")
                    self reply Success
                    
                case None =>
                    self reply RepositoryDoesNotExistError
            }
    }

}
