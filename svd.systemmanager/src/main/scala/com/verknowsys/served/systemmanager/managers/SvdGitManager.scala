package com.verknowsys.served.systemmanager.managers


import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.git
import com.verknowsys.served.api._


/**
 * Git Manager
 * 
 * @author teamon
 */
class SvdGitManager(account: SvdAccount) extends SvdManager(account) {
    
    log.trace("Starting GitManager for account: " + account)

    
    def receive = {
        case Git.ListRepositories =>
            log.trace("Listing git repositories in %s", gitHomeDir)
            self reply Git.Repositories(git.Git.list(gitHomeDir).map(_.name))
            
        case Git.ShowRepository(name) =>
            log.warn("Unimplemented yet!") // TODO: NIY
            self reply NotImplementedError
            
        case Git.CreateRepository(name) =>
            if(SvdUtils.fileExists(gitHomeDir / name + ".git")) {
                self reply Git.RepositoryExistsError
            } else {
                log.trace("Creating new git repository: %s for account: %s".format(name, account.userName))
                git.Git.init(gitHomeDir / name, bare = true)
                if (SvdUtils.chown(gitHomeDir / name, account.uid))
                    log.debug("Chowning created git repository.")
                else
                    log.warn("Couldn't chown created git repository.")
                
                self reply Success
            }
            
        case Git.RemoveRepository(name) =>
            if(SvdUtils.fileExists(gitHomeDir / name + ".git")) {
                log.trace("Removing git repository: %s for account: %s".format(name, account.userName))
                SvdUtils.rmdir(gitHomeDir / name + ".git")
                self reply Success
            } else {
                self reply Git.RepositoryDoesNotExistError
            }
        
        case msg => log.warn("Message not recoginzed: %s", msg)
    }

    
    protected lazy val gitHomeDir = account.homeDir / "git"
    
}
