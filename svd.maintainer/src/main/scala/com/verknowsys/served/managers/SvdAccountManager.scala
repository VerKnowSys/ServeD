package com.verknowsys.served.managers

import com.verknowsys.served.maintainer.SvdAccount
import com.verknowsys.served.utils._
import com.verknowsys.served.api._
import akka.actor.Actor
import akka.util.Logging

case object GetAccount

/**
 * Account Manager - owner of all managers
 * 
 * @author teamon
 */
class SvdAccountManager(val account: SvdAccount) extends Actor with Logging with SvdExceptionHandler {
    log.trace("Starting AccountManager for account: " + account)
    
    def receive = {
        case GetAccount => 
            self reply account
            
        case Git.ListRepositories =>
            log.trace(gitHomeDir)
            self reply Git.Repositories(git.Git.list(gitHomeDir).map(_.name))
            
        case Git.CreateRepository(name) =>
            if(SvdUtils.fileExists(gitHomeDir / name)) {
                self reply Git.RepositoryExistsError
            } else {
                git.Git.init(gitHomeDir / name, bare = true)
                self reply Success
            }
            
        case Git.RemoveRepository(name) =>
            if(SvdUtils.fileExists(gitHomeDir / name)) {
                SvdUtils.rmdir(gitHomeDir / name)
                self reply Success
            } else {
                self reply Git.RepositoryDoesNotExistError
            }
        
        case msg => log.warn("Message net recoginzed: %s", msg)
    }
    
    def gitHomeDir = account.homeDir / "git"

}
