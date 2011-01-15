package com.verknowsys.served.utils

import com.verknowsys.served.SpecHelpers._
import org.specs._
import scala.collection.mutable.ListBuffer

class TestLoggerOutput extends LoggerOutput {
    val logged = new ListBuffer[String]
    
    def log(sender: AnyRef, msg: Logger.Message){
        logged += msg.content
    }
}

class TestLogged extends Logged {
    def logAll {
        trace("test trace msg")
        debug("test debug msg")
        info("test info msg")
        warn("test warn msg")
        error("test error msg")
    }
    
    def logTrace(msg: String) = trace(msg)
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
            output.logged must contain("test trace msg")
            output.logged must contain("test debug msg")
            output.logged must contain("test info msg")
            output.logged must contain("test warn msg")
            output.logged must contain("test error msg")
        }
        
        "log Debug messages" in {
            Logger.level = Logger.Level.Debug
            tester.logAll
            waitWhileRunning(Logger)
            output.logged must not contain("test trace msg")
            output.logged must contain("test debug msg")
            output.logged must contain("test info msg")
            output.logged must contain("test warn msg")
            output.logged must contain("test error msg")
        }
        
        "log Info messages" in {
            Logger.level = Logger.Level.Info
            tester.logAll
            waitWhileRunning(Logger)
            output.logged must not contain("test trace msg")
            output.logged must not contain("test debug msg")
            output.logged must contain("test info msg")
            output.logged must contain("test warn msg")
            output.logged must contain("test error msg")
        }
        
        "log Warn messages" in {
            Logger.level = Logger.Level.Warn
            tester.logAll
            waitWhileRunning(Logger)
            output.logged must not contain("test trace msg")
            output.logged must not contain("test debug msg")
            output.logged must not contain("test info msg")
            output.logged must contain("test warn msg")
            output.logged must contain("test error msg")
        }
        
        "log Error messages" in {
            Logger.level = Logger.Level.Error
            tester.logAll
            waitWhileRunning(Logger)
            output.logged must not contain("test trace msg")
            output.logged must not contain("test debug msg")
            output.logged must not contain("test info msg")
            output.logged must not contain("test warn msg")
            output.logged must contain("test error msg")
        }
        
        // "make use of format" in {
        //     Logger.level = Logger.Level.Trace
        //     Logger.format = "<%{c}> %{m}"
        //     tester.logTrace("some message")
        //     waitWhileRunning(Logger)
        //     output.loggedStrings must contain("<com.verknowsys.served.utils.TestLogged> some message")
        //     
        //     Logger.format = "%{m} (logged by %{c})"
        //     tester.logTrace("Hello logger!")
        //     waitWhileRunning(Logger)
        //     output.loggedStrings must contain("Hello logger! (logged by com.verknowsys.served.utils.TestLogged)")
        // }
    }

}
