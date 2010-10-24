package com.verknowsys.served.managers

import com.verknowsys.served.utils.Utils
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.maintainer.Account
import com.verknowsys.served.utils.git.GitRepository
import com.verknowsys.served.api._
import com.verknowsys.served.api.Git._

import java.io.File


/**
 * Git Manager
 * 
 * @author teamon
 */
class GitManager(owner: AccountManager) extends Manager(owner) {
    def act {
        loop {
            receive {
                // Creates new bare git repository under HOME/git/REPO_NAME
                case CreateRepository(name) => 
                    logger.trace("Creating new git repository %s for account %s".format(name, account.userName))
                    GitRepository.create(gitDir + name, bare = true)
                    sender ! Success
                    
                case RemoveRepository(name) =>
                    logger.warn("Unimplemented yet!")
                    sender ! NotImplementedError
                
                case ListRepositories =>
                    logger.warn("Unimplemented yet!")
                    sender ! NotImplementedError
                
                case ShowRepository(name) =>
                    logger.warn("Unimplemented yet!")
                    sender ! NotImplementedError
                
                case Init =>
                    logger.info("GitManager ready for tasks")
                    
                case Quit =>
                    logger.info("Quitting GitManager...")
                
                case x: AnyRef =>
                    logger.warn("Command not recognized. GitManager will ignore it: " + x.toString)
            }
        }
    }
    
    
    /**
     * Returns list of git repositories
     * 
     * @author teamon
     */
    def repositories = {
        val list = new File(gitDir).list
        if(list == null) List()
        else list.toList
    }
    
    protected lazy val gitDir = account.homeDir + "git/"
}