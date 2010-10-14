package com.verknowsys.served.managers

import scala.actors.Actor
import com.verknowsys.served.utils.Utils
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.maintainer.Account
import com.verknowsys.served.utils.git.GitRepository

import java.io.File

case class CreateRepository(account: Account, name: String)
case class RemoveRepository(account: Account, name: String)
case class ListRepositories(account: Account)
case class ShowRepository(account: Account, name: String)


class GitManager(account: Account) extends Utils {
    def repositories = {
        val list = new File(gitDir).list
        if(list == null) List()
        else list.toList
    }

    protected lazy val gitDir = account.homeDir + "git/"

    def createRepository(name: String) = {
        logger.trace("Creating new git repository %s for account %s".format(name, account.userName))
        GitRepository.create(gitDir + name + ".git", bare = true)
    }
}

object GitManager extends Actor with Utils {
    start
    
    def act {
        loop {
            receive {
                case CreateRepository(account, name) => new GitManager(account).createRepository(name)
                
                
                case Init =>
                    logger.info("GitManager ready for tasks")
                    
                case Quit =>
                    logger.info("Quitting GitManager...")
                
                case x: AnyRef =>
                    logger.warn("Command not recognized. GitManager will ignore it: " + x.toString)
            }
        }
    }
}