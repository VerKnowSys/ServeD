package com.verknowsys.served.systemmanager.managers


import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.systemmanager.managers._
import com.verknowsys.served.SvdSpecHelpers._
import com.verknowsys.served.utils._
import com.verknowsys.served.api._

import akka.actor.Actor.{actorOf, registry}
import akka.actor.ActorRef
import akka.testkit.TestKit
import org.specs._


class SvdGitManagerTest extends Specification with TestKit {

    val homeDir = testPath("home/teamon")
    val account = new SvdAccount(userName = "teamon", homeDir = homeDir)
    var gitm: ActorRef = null
    
    "SvdGitManager" should {
        doBefore {
            gitm = actorOf(new SvdGitManager(account)).start
            mkdir(homeDir)
        }
        
        doAfter {
            registry.shutdownAll
            rmdir(homeDir)
        }
        
        "return empty repository list" in {
            gitm ! Git.ListRepositories
            expectMsg(Git.Repositories(Nil))
        }
        
        "create new bare repository under git directory" in {
            gitm ! Git.CreateRepository("foo")
            expectMsg(Success)
            
            homeDir / "git" must beADirectoryPath
            homeDir / "git" / "foo.git" must beADirectoryPath
            
            new git.GitRepository(homeDir / "git" / "foo.git").isBare must beTrue
            
            gitm ! Git.ListRepositories
            expectMsg(Git.Repositories("foo" :: Nil))
        }
        
        "do not allow creating repository with existing name" in {
            gitm ! Git.CreateRepository("foo")
            expectMsg(Success)
            
            gitm ! Git.CreateRepository("foo")
            expectMsg(Git.RepositoryExistsError)
        }
        
        "remove repository" in {
            gitm ! Git.CreateRepository("foo")
            expectMsg(Success)
            
            gitm ! Git.ListRepositories
            expectMsg(Git.Repositories("foo" :: Nil))
            
            gitm ! Git.RemoveRepository("foo")
            expectMsg(Success)
            
            gitm ! Git.ListRepositories
            expectMsg(Git.Repositories(Nil))
        }
        
        "raise error when removing non existing repository" in {
            gitm ! Git.RemoveRepository("foo")
            expectMsg(Git.RepositoryDoesNotExistError)
        }
    }
}
