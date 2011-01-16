package com.verknowsys.served.utils

import com.verknowsys.served.Config
import com.verknowsys.served.SpecHelpers._
import org.specs._
import scala.collection.mutable.ListBuffer

class TestLoggerOutput extends LoggerOutput {
    val logged = new ListBuffer[String]
    
    def log(sender: AnyRef, msg: Logger.Message){
        logged += msg.content
    }
}

object TestCaller {
    override def toString = "TestCaller"
}
object TestSender {
    override def toString = "TestSender"
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
    
    def timeout = Thread.sleep(250)
        
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
            waitWhileRunning(Logger)
            tester.logAll
            waitWhileRunning(Logger)
            output.logged must not contain("test trace msg")
            output.logged must not contain("test debug msg")
            output.logged must not contain("test info msg")
            output.logged must not contain("test warn msg")
            output.logged must contain("test error msg")
        }
        
        "respond to properties file change (logger level to trace)" in {
            restoreFile(Config.loggerConfigFile){
                writeFile(Config.loggerConfigFile, "logger.level=trace")
                timeout
                waitWhileRunning(Logger)
                tester.logAll
                waitWhileRunning(Logger)
                output.logged must contain("test trace msg")
                output.logged must contain("test debug msg")
                output.logged must contain("test info msg")
                output.logged must contain("test warn msg")
                output.logged must contain("test error msg")
            }
        }
        
        "repond to properties file change (logger level to error)" in {
            restoreFile(Config.loggerConfigFile){
                writeFile(Config.loggerConfigFile, "logger.level=error")
                timeout
                waitWhileRunning(Logger)
                tester.logAll
                waitWhileRunning(Logger)
                output.logged must not contain("test trace msg")
                output.logged must not contain("test debug msg")
                output.logged must not contain("test info msg")
                output.logged must not contain("test warn msg")
                output.logged must contain("test error msg")
            }
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

    val formatedOutput = new ConsoleLoggerOutput

    "LoggerConsoleOutput" should {
        doBefore {
            Logger.output = formatedOutput
        }
        
        "change formatting (default)" in {
            restoreFile(Config.loggerConfigFile){
                writeFile(Config.loggerConfigFile, "logger.console.format=%{l} [%{s} | %{c}]: %{m}")
                timeout
                waitWhileRunning(Logger)
                formatedOutput.formatMessage(TestSender, Logger.Message(TestCaller, "some message", Logger.Level.Trace)) must beEqual("Trace [TestSender | TestCaller]: some message")
                formatedOutput.formatMessage(TestSender, Logger.Message(TestCaller, "other data", Logger.Level.Warn)) must beEqual("Warn [TestSender | TestCaller]: other data")
            }
        }
        
        "change formatting (mixed)" in {
            restoreFile(Config.loggerConfigFile){
                writeFile(Config.loggerConfigFile, "logger.console.format=(%{l}) %{m} sent by %{c}")
                timeout
                waitWhileRunning(Logger)
                formatedOutput.formatMessage(TestSender, Logger.Message(TestCaller, "some message", Logger.Level.Trace)) must beEqual("(Trace) some message sent by TestCaller")
                formatedOutput.formatMessage(TestSender, Logger.Message(TestCaller, "other data", Logger.Level.Warn)) must beEqual("(Warn) other data sent by TestCaller")
            }
        }
    }
}
