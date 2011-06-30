package com.verknowsys.served

import com.verknowsys.served.utils.LoggerUtils
import com.verknowsys.served.utils.Logging
import com.verknowsys.served.api.Logger

import org.scalatest._
import org.scalatest.matchers._
import akka.testkit.TestKit
import java.io.File
import scala.io.Source


package object testing {
    trait TestLogger {
        LoggerUtils.addEntry("com.verknowsys.served", Logger.Levels.Warn)
    }
    
    trait CustomMatchers {
        self: Assertions =>
        
        class FileExistsMatcher extends Matcher[Any] {
            def apply(left: Any) = left match {
                case path: String => check(new File(path))
                case file: File => check(file)
                case _ => fail(left + " is not String or java.io.File")
            }
            
            def check(file: File) = {
                val fileOrDir = if (file.isFile) "file" else "directory"
                val failureMessageSuffix = fileOrDir + " named " + file.getName + " did not exist"
                val negatedFailureMessageSuffix = fileOrDir + " named " + file.getName + " existed"

                MatchResult(
                    file.exists,
                    "The " + failureMessageSuffix,
                    "The " + negatedFailureMessageSuffix,
                    "the " + failureMessageSuffix,
                    "the " + negatedFailureMessageSuffix
                )
            }
        }

        val exist = new FileExistsMatcher
    }

    trait DefaultTest extends FlatSpec 
                         with ShouldMatchers 
                         with TestKit
                         with TestLogger
                         with OneInstancePerTest
                         with BeforeAndAfterEach
                         with CustomMatchers
                         with Logging


    // Common types and objects mapping
    type Actor = akka.actor.Actor
    val Actor = akka.actor.Actor
    type ActorRef = akka.actor.ActorRef
    
    // Utility methods
    def testPublicKey = Source.fromURL(getClass.getResource("/test_key_rsa.pub")).getLines.mkString("\n")
}
