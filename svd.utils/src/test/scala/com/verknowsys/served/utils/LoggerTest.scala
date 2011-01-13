package com.verknowsys.served.utils

import com.verknowsys.served.SpecHelpers._
import org.specs._
import scala.collection.mutable.ListBuffer

class TestLoggerOutput extends LoggerOutput {
    val logged = new ListBuffer[(String, SvdLogger.Level.Value)]
    
    def loggedStrings = logged.map(_._1)
    
    def log(msg: String, level: SvdLogger.Level.Value){
        logged += ((msg, level))
    }
}

class TestLogged extends SvdLogged {
    def logAll {
        logger.trace("trace msg")
        logger.debug("debug msg")
        logger.info("info msg")
        logger.warn("warn msg")
        logger.error("error msg")
    }
    
    def logTrace(msg: String) = logger.trace(msg)
}


class LoggerTest extends Specification {
    var output = new TestLoggerOutput
    val tester = new TestLogged
    
    "Logger" should {
        doBefore {
            output = new TestLoggerOutput
            SvdLogger.output = output
        }
        
        "log Trace messages" in {
            SvdLogger.level = SvdLogger.Level.Trace
            tester.logAll
            waitWhileRunning(SvdLogger)
            output.logged must haveSize(5)
        }
        
        "log Debug messages" in {
            SvdLogger.level = SvdLogger.Level.Debug
            tester.logAll
            waitWhileRunning(SvdLogger)
            output.logged must haveSize(4)
        }
        
        "log Info messages" in {
            SvdLogger.level = SvdLogger.Level.Info
            tester.logAll
            waitWhileRunning(SvdLogger)
            output.logged must haveSize(3)
        }
        
        "log Warn messages" in {
            SvdLogger.level = SvdLogger.Level.Warn
            tester.logAll
            waitWhileRunning(SvdLogger)
            output.logged must haveSize(2)
        }
        
        "log Error messages" in {
            SvdLogger.level = SvdLogger.Level.Error
            tester.logAll
            waitWhileRunning(SvdLogger)
            output.logged must haveSize(1)
        }
        
        "make use of format" in {
            SvdLogger.level = SvdLogger.Level.Trace
            SvdLogger.format = "<%{c}> %{m}"
            tester.logTrace("some message")
            waitWhileRunning(SvdLogger)
            output.loggedStrings must contain("<com.verknowsys.served.utils.TestLogged> some message")
            
            SvdLogger.format = "%{m} (logged by %{c})"
            tester.logTrace("Hello logger!")
            waitWhileRunning(SvdLogger)
            output.loggedStrings must contain("Hello logger! (logged by com.verknowsys.served.utils.TestLogged)")
        }
    }

}
