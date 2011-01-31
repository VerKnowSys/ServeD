package com.verknowsys.served.managers

import com.verknowsys.served.maintainer.SvdAccount
import com.verknowsys.served.SvdSpecHelpers._
import com.verknowsys.served.spechelpers._
import com.verknowsys.served.utils._
import com.verknowsys.served.api._
import akka.actor.Actor.{actorOf, registry}
import akka.actor.ActorRef
import org.specs._

class SvdGitManagerTest extends Specification with SvdExpectActorSpecification {
    val homeDir = testPath("home/teamon")
    val account = new SvdAccount(userName = "teamon", homeDir = homeDir)
    var gitm: ActorRef = null
    
    "SvdGitManager" should {
        doBefore {
            beforeExpectActor
            gitm = actorOf(new SvdGitManager(account)).start
            mkdir(homeDir)
        }
        
        doAfter {
            afterExpectActor
            registry.shutdownAll
            rmdir(homeDir)
        }
        
        "return empty repository list" in {
            gitm ! Git.ListRepositories
            expectActor ? Git.Repositories(Nil)
        }
        
        "create new bare repository under git directory" in {
            gitm ! Git.CreateRepository("foo")
            expectActor ? Success
            
            homeDir / "git" must beADirectoryPath
            homeDir / "git" / "foo.git" must beADirectoryPath
            
            new git.GitRepository(homeDir / "git" / "foo.git").isBare must beTrue
            
            gitm ! Git.ListRepositories
            expectActor ? Git.Repositories("foo" :: Nil)
        }
        
        "do not allow creating repository with existing name" in {
            gitm ! Git.CreateRepository("foo")
            expectActor ? Success
            
            gitm ! Git.CreateRepository("foo")
            expectActor ? Git.RepositoryExistsError
        }
        
        "remove repository" in {
            gitm ! Git.CreateRepository("foo")
            expectActor ? Success
            
            gitm ! Git.ListRepositories
            expectActor ? Git.Repositories("foo" :: Nil)
            
            gitm ! Git.RemoveRepository("foo")
            expectActor ? Success
            
            gitm ! Git.ListRepositories
            expectActor ? Git.Repositories(Nil)
        }
        
        "raise error when removing non existing repository" in {
            gitm ! Git.RemoveRepository("foo")
            expectActor ? Git.RepositoryDoesNotExistError
        }
    }
}
