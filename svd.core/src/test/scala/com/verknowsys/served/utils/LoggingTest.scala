package com.verknowsys.served.utils

import com.verknowsys.served.testing._
import com.verknowsys.served.api.Logger
import com.verknowsys.served.systemmanager.managers.LoggingManager

class LoggingUtilsTest extends DefaultTest {
    LoggerUtils.removeEntry("com.verknowsys.served")
    LoggerUtils.addEntry("com.verknowsys.served.aaa", Logger.Levels.Trace)
    LoggerUtils.addEntry("com.verknowsys.served.bbb", Logger.Levels.Debug)
    LoggerUtils.addEntry("com.verknowsys.served.ccc", Logger.Levels.Info)
    LoggerUtils.addEntry("com.verknowsys.served.ddd", Logger.Levels.Warn)
    LoggerUtils.addEntry("com.verknowsys.served.eee", Logger.Levels.Error)
    LoggerUtils.addEntry("com.verknowsys.served", Logger.Levels.Error)
    
    it should "return correct level for class" in {
        LoggerUtils.levelFor("com.verknowsys.served") should be(Logger.Levels.Error)
        LoggerUtils.levelFor("com.verknowsys.served.aaa") should be(Logger.Levels.Trace)
        LoggerUtils.levelFor("com.verknowsys.served.aaa.sub") should be(Logger.Levels.Trace)
        LoggerUtils.levelFor("com.verknowsys.served.bbb") should be(Logger.Levels.Debug)
        LoggerUtils.levelFor("com.verknowsys.served.bbb.sub") should be(Logger.Levels.Debug)
        LoggerUtils.levelFor("com.verknowsys.served.ccc") should be(Logger.Levels.Info)
        LoggerUtils.levelFor("com.verknowsys.served.ccc.sub") should be(Logger.Levels.Info)
        LoggerUtils.levelFor("com.verknowsys.served.ddd") should be(Logger.Levels.Warn)
        LoggerUtils.levelFor("com.verknowsys.served.ddd.sub") should be(Logger.Levels.Warn)
        LoggerUtils.levelFor("com.verknowsys.served.eee") should be(Logger.Levels.Error)
        LoggerUtils.levelFor("com.verknowsys.served.eee.sub") should be(Logger.Levels.Error)
        LoggerUtils.levelFor("com.verknowsys.served.fff") should be(Logger.Levels.Error) // as in .served
        LoggerUtils.levelFor("com.verknowsys") should be(Logger.Levels.Trace) // default
    }
}

class LoggingManagerTest extends DefaultTest {
    LoggerUtils.clear
    
    val ref = Actor.actorOf[LoggingManager].start
    
    it should "list logger entries" in {
        (ref !! Logger.ListEntries) should be (Some(Logger.Entries(Map())))
        ref.stop
    }
    
    it should "add entry" in {
        ref !! Logger.AddEntry("com.verknowsys.served", Logger.Levels.Trace)
        val res1 = ref !! Logger.ListEntries
        res1 should be(Some(Logger.Entries(Map("com.verknowsys.served" -> Logger.Levels.Trace))))
        LoggerUtils.levelFor("com.verknowsys.served") should be(Logger.Levels.Trace)
        
        ref !! Logger.AddEntry("com.verknowsys.served", Logger.Levels.Error)
        val res2 = ref !! Logger.ListEntries
        res2 should be(Some(Logger.Entries(Map("com.verknowsys.served" -> Logger.Levels.Error))))
        LoggerUtils.levelFor("com.verknowsys.served") should be(Logger.Levels.Error)
        
        ref !! Logger.AddEntry("com.verknowsys.served.foobar", Logger.Levels.Warn)
        val res3 = ref !! Logger.ListEntries
        res3 should be(Some(Logger.Entries(Map(
            "com.verknowsys.served" -> Logger.Levels.Error,
            "com.verknowsys.served.foobar" -> Logger.Levels.Warn
        ))))
        LoggerUtils.levelFor("com.verknowsys.served") should be(Logger.Levels.Error)
        LoggerUtils.levelFor("com.verknowsys.served.foobar") should be(Logger.Levels.Warn)
        
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
