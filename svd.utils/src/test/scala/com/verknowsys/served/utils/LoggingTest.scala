/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.utils

import com.verknowsys.served.testing._
import com.verknowsys.served.api.Logger

object TestLogger extends LoggingMachine

class LoggingUtilsTest extends DefaultTest {
    TestLogger.removeEntry("com.verknowsys.served")
    TestLogger.addEntry("com.verknowsys.served.aaa", Logger.Levels.Trace)
    TestLogger.addEntry("com.verknowsys.served.bbb", Logger.Levels.Debug)
    TestLogger.addEntry("com.verknowsys.served.ccc", Logger.Levels.Info)
    TestLogger.addEntry("com.verknowsys.served.ddd", Logger.Levels.Warn)
    TestLogger.addEntry("com.verknowsys.served.eee", Logger.Levels.Error)
    TestLogger.addEntry("com.verknowsys.served", Logger.Levels.Error)

    it should "return correct level for class" in {
        TestLogger.levelFor("com.verknowsys.served") should be(Logger.Levels.Error)
        TestLogger.levelFor("com.verknowsys.served.aaa") should be(Logger.Levels.Trace)
        TestLogger.levelFor("com.verknowsys.served.aaa.sub") should be(Logger.Levels.Trace)
        TestLogger.levelFor("com.verknowsys.served.bbb") should be(Logger.Levels.Debug)
        TestLogger.levelFor("com.verknowsys.served.bbb.sub") should be(Logger.Levels.Debug)
        TestLogger.levelFor("com.verknowsys.served.ccc") should be(Logger.Levels.Info)
        TestLogger.levelFor("com.verknowsys.served.ccc.sub") should be(Logger.Levels.Info)
        TestLogger.levelFor("com.verknowsys.served.ddd") should be(Logger.Levels.Warn)
        TestLogger.levelFor("com.verknowsys.served.ddd.sub") should be(Logger.Levels.Warn)
        TestLogger.levelFor("com.verknowsys.served.eee") should be(Logger.Levels.Error)
        TestLogger.levelFor("com.verknowsys.served.eee.sub") should be(Logger.Levels.Error)
        TestLogger.levelFor("com.verknowsys.served.fff") should be(Logger.Levels.Error) // as in .served
        TestLogger.levelFor("com.verknowsys") should be(Logger.Levels.Trace) // default
    }
}
