package com.verknowsys.served.utils

import org.specs._
import scala.collection.mutable.ListBuffer

class TestLoggerOutput extends LoggerOutput {
    val logged = new ListBuffer[(String, SvdLogger.Level.Value)]
    
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
            output.logged must haveSize(5)
        }
        
        "log Debug messages" in {
            SvdLogger.level = SvdLogger.Level.Debug
            tester.logAll
            output.logged must haveSize(4)
        }
        
        "log Info messages" in {
            SvdLogger.level = SvdLogger.Level.Info
            tester.logAll
            output.logged must haveSize(3)
        }
        
        "log Warn messages" in {
            SvdLogger.level = SvdLogger.Level.Warn
            tester.logAll
            output.logged must haveSize(2)
        }
        
        "log Error messages" in {
            SvdLogger.level = SvdLogger.Level.Error
            tester.logAll
            output.logged must haveSize(1)
        }
        
    }
}
