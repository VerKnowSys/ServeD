package com.verknowsys.served.utils

import com.verknowsys.served.testing._
import com.verknowsys.served.api.Logger
import com.verknowsys.served.systemmanager.managers.LoggingManager

object TestLogger extends LoggingMachine

class LoggingManagerTest extends DefaultTest {
    TestLogger.clear

    val ref = Actor.actorOf(new LoggingManager(TestLogger)).start

    it should "list logger entries" in {
        (ref !! Logger.ListEntries) should be (Some(Logger.Entries(Map())))
        ref.stop
    }
    
    it should "add entry" in {
        ref !! Logger.AddEntry("com.verknowsys.served", Logger.Levels.Trace)
        val res1 = ref !! Logger.ListEntries
        res1 should be(Some(Logger.Entries(Map("com.verknowsys.served" -> Logger.Levels.Trace))))
        TestLogger.levelFor("com.verknowsys.served") should be(Logger.Levels.Trace)

        ref !! Logger.AddEntry("com.verknowsys.served", Logger.Levels.Error)
        val res2 = ref !! Logger.ListEntries
        res2 should be(Some(Logger.Entries(Map("com.verknowsys.served" -> Logger.Levels.Error))))
        TestLogger.levelFor("com.verknowsys.served") should be(Logger.Levels.Error)

        ref !! Logger.AddEntry("com.verknowsys.served.foobar", Logger.Levels.Warn)
        val res3 = ref !! Logger.ListEntries
        res3 should be(Some(Logger.Entries(Map(
            "com.verknowsys.served" -> Logger.Levels.Error,
            "com.verknowsys.served.foobar" -> Logger.Levels.Warn
        ))))
        TestLogger.levelFor("com.verknowsys.served") should be(Logger.Levels.Error)
        TestLogger.levelFor("com.verknowsys.served.foobar") should be(Logger.Levels.Warn)

        ref.stop
    }
    
    it should "remove entry" in {
        ref !! Logger.AddEntry("com.verknowsys.served.a", Logger.Levels.Trace)
        ref !! Logger.AddEntry("com.verknowsys.served.b", Logger.Levels.Info)
        ref !! Logger.AddEntry("com.verknowsys.served.c", Logger.Levels.Error)
        ref !! Logger.RemoveEntry("com.verknowsys.served.b")
        val res1 = ref !! Logger.ListEntries
        res1 should be(Some(Logger.Entries(Map(
            "com.verknowsys.served.a" -> Logger.Levels.Trace,
            "com.verknowsys.served.c" -> Logger.Levels.Error
        ))))
    }
}
