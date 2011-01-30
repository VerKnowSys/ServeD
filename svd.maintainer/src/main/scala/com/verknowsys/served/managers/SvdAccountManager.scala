package com.verknowsys.served.managers

import com.verknowsys.served.maintainer.SvdAccount
import com.verknowsys.served.utils.SvdExceptionHandler
import com.verknowsys.served.utils.SvdUtils
import com.verknowsys.served.utils.git
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
            self reply Git.Repositories(git.Git.list(account.homeDir + "/git").map(r => Git.Repository(r.name)))
            
        case Git.CreateRepository(name) =>
            if(SvdUtils.fileExists(account.homeDir + "/git/" + name)) {
                self reply Git.RepositoryExistsError
            } else {
                git.Git.init(account.homeDir + "/git/" + name)
                self reply Success
            }
            
        case Git.RemoveRepository(name) =>
            if(SvdUtils.fileExists(account.homeDir + "/git/" + name)) {
                SvdUtils.rmdir(account.homeDir + "/git/" + name)
                self reply Success
            } else {
                self reply Git.RepositoryDoesNotExistError
            }
        
        case msg => log.warn("Message net recoginzed: %s", msg)
    }

}
