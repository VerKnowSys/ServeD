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
            self reply Repositories(RepositoryDB(db).toList)
            
        // case Git.ShowRepository(name) =>
        //     log.warn("Unimplemented yet!") // TODO: NIY
        //     self reply NotImplementedError
        //     
        case CreateRepository(name) =>
            if(SvdUtils.fileExists(gitHomeDir / name + ".git")) {
                self reply RepositoryExistsError
            } else {
                log.trace("Creating new git repository: %s for account: %s".format(name, account.userName))
                val repo = Repository(name)
                Git.init(gitHomeDir / name, bare = true)
                db << repo
                self reply Success
            }
        //     
        // case Git.RemoveRepository(name) =>
        //     if(SvdUtils.fileExists(gitHomeDir / name + ".git")) {
        //         log.trace("Removing git repository: %s for account: %s".format(name, account.userName))
        //         SvdUtils.rmdir(gitHomeDir / name + ".git")
        //         self reply Success
        //     } else {
        //         self reply Git.RepositoryDoesNotExistError
        //     }
    }

    
    protected lazy val gitHomeDir = SvdUtils.checkOrCreateDir(account.homeDir / "git")
}
