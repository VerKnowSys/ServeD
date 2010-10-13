package com.verknowsys.served.maintainer

import com.verknowsys.served.utils.Utils
import com.verknowsys.served.utils.git.GitRepository
import java.io.File
import org.apache.commons.io.FileUtils

case class Account(
        val userName: String = "guest",
        val pass: String = "x",
        val uid: String = "1000",
        val gid: String = "1000",
        val information: String = "No information",
        val homeDir: String = "/home/",
        val shell: String = "/bin/bash"
        ) extends Utils {
            
    def this(a: List[String]) = this(
        userName = a(0),
        pass = a(1),
        uid = a(2),
        gid = a(3),
        information = a(4),
        homeDir = a(5),
        shell = a(6)
    )
    
    
    // def this(a: List[String]) = this(a(0), a(1), a(2), a(3), a(4), a(5), a(6))

    def size = {
        try {
            val elementsSize = FileUtils.sizeOfDirectory(new File(homeDir))
            logger.debug("getAccountSize of " + homeDir + " folder: " + elementsSize)
            Some(elementsSize)
        } catch {
            case x: Exception =>
                logger.error("Error: " + x)
                None
        }
    }
    
    
    // Git related stuff

    def repositories = {
        val list = new File(gitDir).list
        if(list == null) List()
        else list.toList
    }
    
    protected lazy val gitDir = homeDir + "git/"
    
    def createRepository(name: String) = {
        logger.trace("Creating new git repository %s for account %s".format(name, userName))
        GitRepository.create(gitDir + name + ".git", bare = true)
    }
    
    // TODO def setupGitWatchers

}
