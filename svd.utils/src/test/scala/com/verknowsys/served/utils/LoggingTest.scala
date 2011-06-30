package com.verknowsys.served.utils

import com.verknowsys.served.testing._
import com.verknowsys.served.api.Logger

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
