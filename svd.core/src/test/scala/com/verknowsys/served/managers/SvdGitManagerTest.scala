package com.verknowsys.served.managers

import com.verknowsys.served.testing._
import com.verknowsys.served._
import Actor._
import com.verknowsys.served.SvdSpecHelpers._
import com.verknowsys.served.systemmanager.managers._
import com.verknowsys.served.utils._
import com.verknowsys.served.git._
import com.verknowsys.served.api.git._
import com.verknowsys.served.api.Success
import com.verknowsys.served.db._
import akka.util.duration._


class SvdGitManagerTest extends DefaultTest {

    val account = currentAccount
    val homeDir = tmpDir / account.uid.toString
    val dbServer = new DBServer(randomPort, randomPath)
    val db = dbServer.openClient
    val manager = actorOf(new SvdGitManager(account, db, homeDir / "git")).start
    mkdir(homeDir / "git")

    override def afterEach {
        manager.stop
        db.close
        dbServer.close
        rmdir(homeDir / "git")
    }

    it should "return empty repository list" in {
        manager ! ListRepositories

        within(3 seconds){
            expectMsg(Repositories(Nil))
        }
    }

    it should "create new bare repository under git directory" in {
        manager ! CreateRepository("foo")
        val repo = within(3 seconds){
            val repo = expectMsgClass(classOf[Repository])
            repo.name should be("foo")
            repo.authorizedKeys should be ('empty)
            repo
        }

        homeDir / "git" should (exist)
        homeDir / "git" / "foo.git" should (exist)

        new GitRepository(homeDir / "git" / "foo.git") should be ('bare)

        manager ! ListRepositories
        within(3 seconds){
            expectMsg(Repositories(repo :: Nil))
        }
    }

    it should "do not allow creating repository with existing name" in {
        manager ! CreateRepository("foo")
        within(3 seconds){
            val repo = expectMsgClass(classOf[Repository])
            repo.name should equal ("foo")
            repo.authorizedKeys should be ('empty)
            repo
        }

        manager ! CreateRepository("foo")
        expectMsg(RepositoryExistsError)
    }

    it should "remove repository" in {
        manager ! CreateRepository("foo")
        val foo = within(3 second){
             expectMsgClass(classOf[Repository])
        }

        manager ! RemoveRepository(foo.uuid)

        within(3 second){
             expectMsg(Success)
        }
        homeDir / "git" / "foo.git" should not (exist)

        manager ! ListRepositories
        within(3 second){
             expectMsg(Repositories(Nil))
        }
    }
    
    ignore should "retrieve repository by name" in {
        manager ! CreateRepository("foo")
        val foo = within(3 second){
             expectMsgClass(classOf[Repository])
        }

        manager ! GetRepositoryByName("foo")
        within(3 second){
             expectMsg(Some(foo))
        }
        
        manager ! GetRepositoryByName("blaaah")
        within(3 second){
             expectMsg(None)
        }
    }
    
    ignore should "retrieve repository by uuid" in {
        manager ! CreateRepository("foo")
        val foo = within(3 second){
             expectMsgClass(classOf[Repository])
        }

        manager ! GetRepositoryByUUID(foo.uuid)
        within(3 second){
             expectMsg(Some(foo))
        }
        
        manager ! GetRepositoryByUUID(java.util.UUID.randomUUID)
        within(3 second){
             expectMsg(None)
        }
    }

    it should "raise error when removing non existing repository" in {
        manager ! RemoveRepository(java.util.UUID.randomUUID)
        within(3 seconds){
            expectMsg(RepositoryDoesNotExistError)
        }
    }
}
