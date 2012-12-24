package com.verknowsys.served.managers


import akka.pattern.ask
import akka.testkit.TestKit
import akka.actor.{ActorSystem, Props}

import com.verknowsys.served.utils._
import com.verknowsys.served.git._
import com.verknowsys.served.db._
import com.verknowsys.served.api._
import com.verknowsys.served.api.git._
import com.verknowsys.served.testing._


/**
*   @author dmilith
*   @author teamon
*
*/

class SvdGitManagerTest(_system: ActorSystem) extends TestKit(_system) with DefaultTest {

    def this() = this(ActorSystem("svd-test-system"))
    // val ref = system.actorOf(Props(new LoggingManager(GlobalLogger)))

    val account = currentAccount
    val homeDir = tmpDir / account.uid.toString
    val dbServer = new DBServer(randomPort, randomPath)
    val db = dbServer.openClient
    var manager: ActorRef = null


    override def afterAll {
        // system.stop(manager)
        // db.close
        // dbServer.close
        rmdir(homeDir / "git")
        system.shutdown
    }


    override def afterEach {
        // system.stop(manager)
        system.stop(manager)
        // rmdir(homeDir / "git")
    }


    override def beforeEach {
        mkdir(homeDir / "git")
        manager = system.actorOf(Props(new SvdGitManager(account, db, homeDir / "git")))
    }


    it should "return empty repository list" in {
        (manager ? ListRepositories) onSuccess {
            case Repositories(x) =>
                x must be(Nil)
        }
    }


    it should "create new bare repository under git directory" in {
        (manager ? CreateRepository("foo")) onSuccess {
            case repo: Repository =>
                repo.name should be("foo")
                repo.authorizedKeys should be ('empty)
                homeDir / "git" should (exist)
                homeDir / "git" / "foo.git" should (exist)

                (manager ? ListRepositories) onSuccess {
                    case x: Repositories =>
                        x must be(repo :: Nil)
                        // make sure it's bare repo:
                        new GitRepository(homeDir / "git" / "foo.git") should be ('bare)
                }
        }
    }


    it should "do not allow creating repository with existing name" in {
        (manager ? CreateRepository("foo")) onSuccess {
            case repo: Repository =>
                repo.name should be ("foo")
                repo.authorizedKeys should be ('empty)
                (manager ? CreateRepository("foo")) onSuccess {
                    case RepositoryExistsError =>
                        true must be(true)
                    case _ =>
                        fail("Shouldn't happen")
                }
        }
    }


    it should "remove repository" in {
        (manager ? CreateRepository("foo")) onSuccess {
            case repo: Repository =>
                (manager ? RemoveRepository(repo.uuid)) onSuccess {
                    case Success =>
                        homeDir / "git" / "foo.git" should not (exist)
                        (manager ? ListRepositories) onSuccess {
                            case Repositories(Nil) =>
                                true must be(true)

                            case _ =>
                                fail("Shouldn't happen")
                        }
                    case _ =>
                        fail("Shouldn't happen")
                }

            case _ =>
                fail("Shouldn't happen")
        }
    }


    it should "retrieve repository by name" in {
        (manager ? CreateRepository("foo")) onSuccess {
            case repo: Repository =>
                (manager ? GetRepositoryByName("foo")) onSuccess {
                    case Some(x: Repository) =>
                        (manager ? GetRepositoryByName("blaaah")) onSuccess {
                            case Nil =>
                                true must be(true)

                            case _ =>
                                fail("Shouldn't happen")
                        }
                    case _ =>
                        fail("Shouldn't happen")
                }

            case _ =>
                fail("Shouldn't happen")
        }
    }


    it should "retrieve repository by uuid" in {
        (manager ? CreateRepository("foo")) onSuccess {
            case repo: Repository =>
                (manager ? GetRepositoryByUUID(repo.uuid)) onSuccess {
                    case Some(x: Repository) =>
                        (manager ? GetRepositoryByUUID(java.util.UUID.randomUUID)) onSuccess {
                            case None =>
                                true must be(true)

                            case _ =>
                                fail("Shouldn't happen")
                        }

                    case _ =>
                        fail("Shouldn't happen")
                }

            case _ =>
                fail("Shouldn't happen")
        }
    }


    it should "raise error when removing non existing repository" in {
        (manager ? RemoveRepository(java.util.UUID.randomUUID)) onSuccess {
            case RepositoryDoesNotExistError =>
                true must be(true)
            case _ =>
                fail("Shouldn't happen")
        }
    }


    it should "add new key to config" in {
        (manager ? CreateRepository("foo")) onSuccess {
            case repo: Repository =>

                val key = AccessKey("default", KeyUtils.load(testPublicKey).get) // we are sure this is valid key
                (manager ? AddAuthorizedKey(repo.uuid, key)) onSuccess {
                    case Success =>
                        (manager ? GetRepositoryByUUID(repo.uuid)) onSuccess {
                            case Some(res: Repository) =>
                                res.authorizedKeys should have size(1)
                                res.authorizedKeys should contain (key)
                                res should equal (repo.copy(authorizedKeys = Set() + key))

                                manager ! AddAuthorizedKey(repo.uuid, key)
                                expectMsg(Success)

                                (manager ? GetRepositoryByUUID(repo.uuid)) onSuccess {
                                    case Some(res2: Repository) =>
                                        res2.authorizedKeys should have size(1)
                                        res2.authorizedKeys should contain (key)
                                        res2 should equal (res)

                                    case _ =>
                                        fail("Shouldn't happen")
                                }

                            case _ =>
                                fail("Shouldn't happen")
                        }

                    case _ =>
                        fail("Shouldn't happen")
                }

            case RepositoryExistsError =>
                fail("Shouldn't happen")

        }
    }


    it should "remove key from config" in {
        (manager ? CreateRepository("foo")) onSuccess {
            case repo: Repository =>

                val key = AccessKey("default", KeyUtils.load(testPublicKey).get) // we are sure this is valid key

                (manager ? AddAuthorizedKey(repo.uuid, key)) onSuccess {
                    case Success =>
                    case _ =>
                        fail("Shouldn't happen")
                }

                (manager ? RemoveAuthorizedKey(repo.uuid, key)) onSuccess {
                    case Success =>
                    case _ =>
                        fail("Shouldn't happen")
                }

                (manager ? GetRepositoryByUUID(repo.uuid)) onSuccess {
                    case Some(res: Repository) =>
                        res.authorizedKeys should have size(0)
                        res.authorizedKeys should not contain (key)
                        res should equal (repo.copy(authorizedKeys = Set()))

                    case _ =>
                        fail("Shouldn't happen")
                }

            case _ =>
                fail("Shouldn't happen")
        }
    }


}
