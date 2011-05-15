package com.verknowsys.served.utils


import org.specs._
import akka.testkit.TestKit
import akka.actor.{Actor, ActorRef}

import com.verknowsys.served.SvdConfig
import com.verknowsys.served.SvdSpecHelpers._
import com.verknowsys.served.api.Logger
import com.verknowsys.served.api.Logger

class LoggingTest extends Specification with TestKit {
    "LoggerUtils" should {
        doBefore { 
            setupConfigFile
            LoggerUtils.update
        }
       
        "read logger config from properties file" in {
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
    
    "LoggingManager" should {
        var ref: ActorRef = null
        
        doBefore {
            removeConfigFile
            LoggerUtils.update
            ref = Actor.actorOf[LoggingManager].start()
        }
        
        "List logger entries" in {
            val res = ref !! Logger.ListEntries
            res.get must_== Logger.Entries(Map())
        }
        
        "Add entry" in {
            ref !! Logger.AddEntry("com.verknowsys.served", Logger.Levels.Trace)
            val res1 = ref !! Logger.ListEntries
            res1.get must_== Logger.Entries(Map("com.verknowsys.served" -> Logger.Levels.Trace))
            LoggerUtils.levelFor("com.verknowsys.served") must_== Logger.Levels.Trace
            
            ref !! Logger.AddEntry("com.verknowsys.served", Logger.Levels.Error)
            val res2 = ref !! Logger.ListEntries
            res2.get must_== Logger.Entries(Map("com.verknowsys.served" -> Logger.Levels.Error))
            LoggerUtils.levelFor("com.verknowsys.served") must_== Logger.Levels.Error
            
            ref !! Logger.AddEntry("com.verknowsys.served.foobar", Logger.Levels.Warn)
            val res3 = ref !! Logger.ListEntries
            res3.get must_== Logger.Entries(Map(
                "com.verknowsys.served" -> Logger.Levels.Error,
                "com.verknowsys.served.foobar" -> Logger.Levels.Warn
            ))
            LoggerUtils.levelFor("com.verknowsys.served") must_== Logger.Levels.Error
            LoggerUtils.levelFor("com.verknowsys.served.foobar") must_== Logger.Levels.Warn
        }
        
        "Remove entry" in {
            ref !! Logger.AddEntry("com.verknowsys.served.a", Logger.Levels.Trace)
            ref !! Logger.AddEntry("com.verknowsys.served.b", Logger.Levels.Info)
            ref !! Logger.AddEntry("com.verknowsys.served.c", Logger.Levels.Error)
            ref !! Logger.RemoveEntry("com.verknowsys.served.b")
            val res1 = ref !! Logger.ListEntries
            res1.get must_== Logger.Entries(Map(
                "com.verknowsys.served.a" -> Logger.Levels.Trace,
                "com.verknowsys.served.c" -> Logger.Levels.Error
            ))
            
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
    
    private def removeConfigFile {
        writeFile(SvdConfig.loggerPropertiesFilename, "")
    }
}
       