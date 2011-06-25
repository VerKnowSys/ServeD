package com.verknowsys.served

import com.verknowsys.served.utils.LoggerUtils
import com.verknowsys.served.api.Logger

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import akka.testkit.TestKit

package object testing {
    trait TestLogger {
        LoggerUtils.addEntry("com.verknowsys.served", Logger.Levels.Warn)
    }
    
    trait DefaultTest extends FlatSpec 
                         with ShouldMatchers 
                         with TestKit
                         with TestLogger
                         with OneInstancePerTest
    
    // Common types and objects mapping
    type Actor = akka.actor.Actor
    val Actor = akka.actor.Actor
    type ActorRef = akka.actor.ActorRef
}
