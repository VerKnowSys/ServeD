package com.verknowsys.served.utils

import com.verknowsys.served.SvdConfig

import org.specs._
import com.verknowsys.served.SvdSpecHelpers._
import com.verknowsys.served.api.Logger

class LoggingTest extends Specification {
    "LoggerUtils" should {
        doBefore { 
            setupConfigFile
            LoggerUtils.update
        }
       
        "read logger config from properties file" in {
            println(LoggerUtils.levels)
            
            LoggerUtils.levelFor("com.verknowsys.served") must_== Logger.Levels.Error
            LoggerUtils.levelFor("com.verknowsys.served.aaa") must_== Logger.Levels.Trace
            LoggerUtils.levelFor("com.verknowsys.served.aaa.sub") must_== Logger.Levels.Trace
            LoggerUtils.levelFor("com.verknowsys.served.bbb") must_== Logger.Levels.Debug
            LoggerUtils.levelFor("com.verknowsys.served.bbb.sub") must_== Logger.Levels.Debug
            LoggerUtils.levelFor("com.verknowsys.served.ccc") must_== Logger.Levels.Info
            LoggerUtils.levelFor("com.verknowsys.served.ccc.sub") must_== Logger.Levels.Info
            LoggerUtils.levelFor("com.verknowsys.served.ddd") must_== Logger.Levels.Warn
            LoggerUtils.levelFor("com.verknowsys.served.ddd.sub") must_== Logger.Levels.Warn
            LoggerUtils.levelFor("com.verknowsys.served.eee") must_== Logger.Levels.Error
            LoggerUtils.levelFor("com.verknowsys.served.eee.sub") must_== Logger.Levels.Error
            LoggerUtils.levelFor("com.verknowsys.served.fff") must_== Logger.Levels.Error // as in .served
            LoggerUtils.levelFor("com.verknowsys") must_== Logger.Levels.Debug // default
        }
    }
    
    private def setupConfigFile {
        val content = 
                "com.verknowsys.served.aaa=trace" ::
                "com.verknowsys.served.bbb=debug" ::
                "com.verknowsys.served.ccc=info" ::
                "com.verknowsys.served.ddd=warn" ::
                "com.verknowsys.served.eee=error" ::
                "com.verknowsys.served=error" ::
                Nil mkString "\n"
                
        writeFile(SvdConfig.loggerPropertiesFilename, content)
    }
}
       