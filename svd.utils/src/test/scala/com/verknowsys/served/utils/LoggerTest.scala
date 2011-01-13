package com.verknowsys.served.utils

import com.verknowsys.served.SpecHelpers._
import org.specs._
import scala.collection.mutable.ListBuffer

class TestLoggerOutput extends LoggerOutput {
    val logged = new ListBuffer[(String, Logger.Level.Value)]
    
    def loggedStrings = logged.map(_._1)
    
    def log(msg: String, level: Logger.Level.Value){
        logged += ((msg, level))
    }
}

class TestLogged extends Logged {
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
            Logger.output = output
        }
        
        "log Trace messages" in {
            Logger.level = Logger.Level.Trace
            tester.logAll
            waitWhileRunning(Logger)
            output.logged must haveSize(5)
        }
        
        "log Debug messages" in {
            Logger.level = Logger.Level.Debug
            tester.logAll
            waitWhileRunning(Logger)
            output.logged must haveSize(4)
        }
        
        "log Info messages" in {
            Logger.level = Logger.Level.Info
            tester.logAll
            waitWhileRunning(Logger)
            output.logged must haveSize(3)
        }
        
        "log Warn messages" in {
            Logger.level = Logger.Level.Warn
            tester.logAll
            waitWhileRunning(Logger)
            output.logged must haveSize(2)
        }
        
        "log Error messages" in {
            Logger.level = Logger.Level.Error
            tester.logAll
            waitWhileRunning(Logger)
            output.logged must haveSize(1)
        }
        
        "make use of format" in {
            Logger.level = Logger.Level.Trace
            Logger.format = "<%{c}> %{m}"
            tester.logTrace("some message")
            waitWhileRunning(Logger)
            output.loggedStrings must contain("<com.verknowsys.served.utils.TestLogged> some message")
            
            Logger.format = "%{m} (logged by %{c})"
            tester.logTrace("Hello logger!")
            waitWhileRunning(Logger)
            output.loggedStrings must contain("Hello logger! (logged by com.verknowsys.served.utils.TestLogged)")
        }
    }

}
