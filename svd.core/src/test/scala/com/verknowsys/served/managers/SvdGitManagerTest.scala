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
    
    def newRepo = {
        manager ! CreateRepository("foo")
        within(timeout){
             expectMsgClass(classOf[Repository])
        }
    }
    
    def timeout = 3.seconds

    override def afterEach {
        manager.stop
        db.close
        dbServer.close
        rmdir(homeDir / "git")
    }

    it should "return empty repository list" in {
        manager ! ListRepositories

        within(timeout){
            expectMsg(Repositories(Nil))
        }
    }

    it should "create new bare repository under git directory" in {
        manager ! CreateRepository("foo")
        val repo = within(timeout){
            val repo = expectMsgClass(classOf[Repository])
            repo.name should be("foo")
            repo.authorizedKeys should be ('empty)
            repo
        }

        homeDir / "git" should (exist)
        homeDir / "git" / "foo.git" should (exist)

        new GitRepository(homeDir / "git" / "foo.git") should be ('bare)

        manager ! ListRepositories
        within(timeout){
            expectMsg(Repositories(repo :: Nil))
        }
    }

    it should "do not allow creating repository with existing name" in {
        manager ! CreateRepository("foo")
        within(timeout){
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
        val foo = within(timeout){
             expectMsgClass(classOf[Repository])
        }

        manager ! RemoveRepository(foo.uuid)

        within(timeout){
             expectMsg(Success)
        }
        homeDir / "git" / "foo.git" should not (exist)

        manager ! ListRepositories
        within(timeout){
             expectMsg(Repositories(Nil))
        }
    }
    
    it should "retrieve repository by name" in {
        manager ! CreateRepository("foo")
        val foo = within(timeout){
             expectMsgClass(classOf[Repository])
        }

        manager ! GetRepositoryByName("foo")
        within(timeout){
             expectMsg(Some(foo))
        }
        
        manager ! GetRepositoryByName("blaaah")
        within(timeout){
             expectMsg(None)
        }
    }
    
    it should "retrieve repository by uuid" in {
        manager ! CreateRepository("foo")
        val foo = within(timeout){
             expectMsgClass(classOf[Repository])
        }

        manager ! GetRepositoryByUUID(foo.uuid)
        within(timeout){
             expectMsg(Some(foo))
        }
        
        manager ! GetRepositoryByUUID(java.util.UUID.randomUUID)
        within(timeout){
             expectMsg(None)
        }
    }

    it should "raise error when removing non existing repository" in {
        manager ! RemoveRepository(java.util.UUID.randomUUID)
        within(timeout){
            expectMsg(RepositoryDoesNotExistError)
        }
    }
    
    it should "add new key to config" in {
        val repo = newRepo
        val key = KeyUtils.load(testPublicKey).get // we are sure this is valid key
        
        manager ! AddAuthorizedKey(repo.uuid, key)
        within(timeout){
            expectMsg(Success)
        }
        
        manager ! GetRepositoryByUUID(repo.uuid)
        val res = within(timeout){
            expectMsgClass(classOf[Some[Repository]])
        }.get
        
        res.authorizedKeys should have size(1)
        res.authorizedKeys should contain (key)
        res should equal (repo.copy(authorizedKeys = Set() + key))
        
        manager ! AddAuthorizedKey(repo.uuid, key)
        within(timeout){
            expectMsg(Success)
        }
        
        manager ! GetRepositoryByUUID(repo.uuid)
        val res2 = within(timeout){
            expectMsgClass(classOf[Some[Repository]])
        }.get
        
        res2.authorizedKeys should have size(1)
        res2.authorizedKeys should contain (key)
        res2 should equal (res)
    }
    
    it should "remove key from config" in {
        val repo = newRepo
        val key = KeyUtils.load(testPublicKey).get // we are sure this is valid key
        
        manager ! AddAuthorizedKey(repo.uuid, key)
        within(timeout){
            expectMsg(Success)
        }
        
        manager ! RemoveAuthorizedKey(repo.uuid, key)
        within(timeout){
            expectMsg(Success)
        }
        
        manager ! GetRepositoryByUUID(repo.uuid)
        val res = within(timeout){
            expectMsgClass(classOf[Some[Repository]])
        }.get
        
        res.authorizedKeys should have size(0)
        res.authorizedKeys should not contain (key)
        res should equal (repo.copy(authorizedKeys = Set()))
    }
}
