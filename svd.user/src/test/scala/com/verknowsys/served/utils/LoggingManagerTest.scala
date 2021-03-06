///*
// * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
// * This Software is a close code project. You may not redistribute this code without permission of author.
// */
//
//package com.verknowsys.served.utils
//
//import akka.pattern.ask
//import akka.util.Timeout
//import akka.testkit.TestKit
//import scala.concurrent.duration._
//import akka.actor._
//
//import com.verknowsys.served.api.Logger
//import com.verknowsys.served.managers.LoggingManager
//import com.verknowsys.served.testing._
//
//
//object TestLogger extends LoggingMachine
//
//
//class LoggingManagerTest(_system: ActorSystem) extends TestKit(_system) with DefaultTest {
//
//    def this() = this(ActorSystem("svd-test-system"))
//
//    implicit val timeout = Timeout(30 seconds)
//
//
//    override def afterAll {
//        system.shutdown
//    }
//
//    override def beforeEach {
//        TestLogger.clear
//    }
//
//
//    it should "list logger entries" in {
//        val ref = system.actorOf(Props(new LoggingManager(TestLogger)))
//        (ref ? Logger.ListEntries) onSuccess {
//            case Logger.Entries(x) =>
//                true must be(true)
//            case x =>
//                fail("Shouldn't happen. Problem: %s".format(x))
//        }
//        system.stop(ref)
//    }
//
//
//    it should "add entry and check logger levels" in {
//        val ref = system.actorOf(Props(new LoggingManager(GlobalLogger)))
//        (ref ? Logger.AddEntry("com.verknowsys.served", Logger.Levels.Trace)) onSuccess {
//            case _ =>
//                (ref ? Logger.ListEntries) onSuccess {
//                    case Logger.Entries(x) =>
//
//                        x must be(Map("com.verknowsys.served" -> Logger.Levels.Trace))
//                        TestLogger.levelFor("com.verknowsys.served") should be(Logger.Levels.Trace)
//
//                    case x =>
//                        fail("Shouldn't happen: %s".format(x))
//                }
//        }
//
//        (ref ? Logger.AddEntry("com.verknowsys.served", Logger.Levels.Error)) onSuccess {
//            case _ =>
//                (ref ? Logger.ListEntries) onSuccess {
//                    case Logger.Entries(x) =>
//
//                        Logger.Entries(x) must be(Logger.Entries(Map("com.verknowsys.served" -> Logger.Levels.Error)))
//                        TestLogger.levelFor("com.verknowsys.served") should be(Logger.Levels.Error)
//
//                    case x =>
//                        fail("Shouldn't happen: %s".format(x))
//                }
//        }
//
//        (ref ? Logger.AddEntry("com.verknowsys.served", Logger.Levels.Warn)) onSuccess {
//            case _ =>
//                (ref ? Logger.ListEntries) onSuccess {
//                    case Logger.Entries(x) =>
//
//                        Logger.Entries(x) must be(Logger.Entries(
//                            Map(
//                                "com.verknowsys.served" -> Logger.Levels.Error,
//                                "com.verknowsys.served.foobar" -> Logger.Levels.Warn
//                            )
//                        ))
//                        TestLogger.levelFor("com.verknowsys.served") should be(Logger.Levels.Error)
//                        TestLogger.levelFor("com.verknowsys.served.foobar") should be(Logger.Levels.Warn)
//
//                    case x =>
//                        fail("Shouldn't happen: %s".format(x))
//                }
//        }
//        system.stop(ref)
//    }
//
//
//    it should "remove entry" in {
//        val ref = system.actorOf(Props(new LoggingManager(TestLogger)))
//        (ref ? Logger.AddEntry("com.verknowsys.served.a", Logger.Levels.Trace)) onSuccess {
//            case _ =>
//                (ref ? Logger.AddEntry("com.verknowsys.served.b", Logger.Levels.Info)) onSuccess {
//                    case _ =>
//                        (ref ? Logger.AddEntry("com.verknowsys.served.c", Logger.Levels.Error)) onSuccess {
//                            case _ =>
//                                (ref ? Logger.RemoveEntry("com.verknowsys.served.b")) onSuccess {
//                                        case _ =>
//                                            (ref ? Logger.ListEntries) onSuccess {
//                                                case Logger.Entries(x) =>
//                                                    Logger.Entries(x) must be(Logger.Entries(
//                                                        Map(
//                                                            "com.verknowsys.served.a" -> Logger.Levels.Trace,
//                                                            "com.verknowsys.served.c" -> Logger.Levels.Error
//                                                        )
//                                                    ))
//
//                                                case x =>
//                                                    fail("Shouldn't happen: %s".format(x))
//                                            }
//                                }
//                        }
//                    }
//        }
//        system.stop(ref)
//    }
//
//
//}
