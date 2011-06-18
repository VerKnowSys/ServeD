package com.verknowsys.served.managers

import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.systemmanager.managers._
import com.verknowsys.served.SvdSpecHelpers._
import com.verknowsys.served.utils._
import com.verknowsys.served.git._
import com.verknowsys.served.api.git._
import com.verknowsys.served.api.Success
import com.verknowsys.served.db._

import akka.actor.Actor.{actorOf, registry}
import akka.actor.ActorRef
import akka.testkit.TestKit
import akka.util.duration._
import org.specs._


class SvdGitManagerTest extends Specification with TestKit {
    val account = currentAccount
    val homeDir = account.homeDir
    
    var manager: ActorRef = null
    var db: DBClient = null
    var dbServer: DBServer = null
    
    "SvdGitManager" should {
        doBefore {
            dbServer = new DBServer(randomPort, randomPath)
            db = dbServer.openClient
            manager = actorOf(new SvdGitManager(account, db)).start
            mkdir(homeDir / "git")
        }
        
        doAfter {
            db.close
            dbServer.close
            registry.shutdownAll
            rmdir(homeDir / "git")
        }
        
        "return empty repository list" in {
            manager ! ListRepositories
            
            within(3 seconds){
                expectMsg(Repositories(Nil))
            }
        }
        
        "create new bare repository under git directory" in {
            manager ! CreateRepository("foo")
            val repo = within(3 seconds){
                val repo = expectMsgClass(classOf[Repository])
                repo.name must_== "foo"
                repo.authorizedKeys must beEmpty
                repo
            }
            
            homeDir / "git" must beADirectoryPath
            homeDir / "git" / "foo.git" must beADirectoryPath
            
            new GitRepository(homeDir / "git" / "foo.git").isBare must beTrue
            
            manager ! ListRepositories
            within(3 seconds){
                expectMsg(Repositories(repo :: Nil))
            }
        }
        
        "do not allow creating repository with existing name" in {
            manager ! CreateRepository("foo")
            within(3 seconds){
                val repo = expectMsgClass(classOf[Repository])
                repo.name must_== "foo"
                repo.authorizedKeys must beEmpty
                repo
            }
            
            manager ! CreateRepository("foo")
            expectMsg(RepositoryExistsError)
        }

        "remove repository" in {
            manager ! CreateRepository("foo")
            val foo = within(3 second){
                expectMsgClass(classOf[Repository])
            }
            
            manager ! RemoveRepository(foo.uuid)
            
            within(3 second){
                expectMsg(Success)
            }
            homeDir / "git" / "foo.git" mustNot beADirectoryPath
            
            manager ! ListRepositories
            within(3 second){
                expectMsg(Repositories(Nil))
            }
        }

        "raise error when removing non existing repository" in {
            manager ! RemoveRepository(java.util.UUID.randomUUID)
            within(3 seconds){
                expectMsg(RepositoryDoesNotExistError)
            }
        }
    }
}
