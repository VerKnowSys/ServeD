package com.verknowsys.served.utils

import akka.testkit.TestActorRef
import com.typesafe.config.ConfigFactory
import akka.dispatch._
import akka.pattern.ask
import akka.remote._
import akka.util.Duration
import akka.util.Timeout
import akka.testkit.TestKit
import akka.util.duration._
import akka.actor.ActorSystem
import akka.actor._

import com.verknowsys.served._
import com.verknowsys.served.utils._
import com.verknowsys.served.api.Logger
import com.verknowsys.served.managers.LoggingManager
import com.verknowsys.served.testing._


object TestLogger extends LoggingMachine


class LoggingManagerTest(_system: ActorSystem) extends TestKit(_system) with DefaultTest {
    TestLogger.clear

    def this() = this(ActorSystem("svd-test-system"))


    override def afterAll {
        system.shutdown
    }

    it should "list logger entries" in {
        val ref = system.actorOf(Props(new LoggingManager(TestLogger)))
        ref ! Logger.ListEntries
        expectMsg(Some(Logger.Entries(Map())))
        system.stop(ref)
    }

    it should "add entry" in {
        val ref = system.actorOf(Props(new LoggingManager(GlobalLogger)))
        ref ! Logger.AddEntry("com.verknowsys.served", Logger.Levels.Trace)
        ref ! Logger.ListEntries
        expectMsg(Some(Logger.Entries(Map("com.verknowsys.served" -> Logger.Levels.Trace))))

        TestLogger.levelFor("com.verknowsys.served") should be(Logger.Levels.Trace)

        ref ! Logger.AddEntry("com.verknowsys.served", Logger.Levels.Error)
        val res2 = ref ! Logger.ListEntries
        expectMsg(Some(Logger.Entries(Map("com.verknowsys.served" -> Logger.Levels.Error))))
        TestLogger.levelFor("com.verknowsys.served") should be(Logger.Levels.Error)

        ref ! Logger.AddEntry("com.verknowsys.served.foobar", Logger.Levels.Warn)
        val res3 = ref ! Logger.ListEntries
        expectMsg(Some(Logger.Entries(Map(
            "com.verknowsys.served" -> Logger.Levels.Error,
            "com.verknowsys.served.foobar" -> Logger.Levels.Warn
        ))))
        TestLogger.levelFor("com.verknowsys.served") should be(Logger.Levels.Error)
        TestLogger.levelFor("com.verknowsys.served.foobar") should be(Logger.Levels.Warn)

        system.stop(ref)
    }

    it should "remove entry" in {
        val ref = system.actorOf(Props(new LoggingManager(GlobalLogger)))
        ref ! Logger.AddEntry("com.verknowsys.served.a", Logger.Levels.Trace)
        ref ! Logger.AddEntry("com.verknowsys.served.b", Logger.Levels.Info)
        ref ! Logger.AddEntry("com.verknowsys.served.c", Logger.Levels.Error)
        ref ! Logger.RemoveEntry("com.verknowsys.served.b")
        ref ! Logger.ListEntries
        expectMsg(Some(Logger.Entries(Map(
            "com.verknowsys.served.a" -> Logger.Levels.Trace,
            "com.verknowsys.served.c" -> Logger.Levels.Error
        ))))
        system.stop(ref)
    }
}
