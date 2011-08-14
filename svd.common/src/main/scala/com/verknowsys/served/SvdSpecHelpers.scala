package com.verknowsys.served

import com.verknowsys.served.utils._
import com.verknowsys.served._

import org.apache.commons.io.FileUtils
import java.io.File
import akka.actor.ActorRef
import com.verknowsys.served.api.SvdAccount
import scala.io.Source


@deprecated("SvdSpecHelpers should be moved to testing", "2011-07-02")
object SvdSpecHelpers {
    implicit def ItemToSeq[T](a: T) = a :: Nil


    val usedPorts = scala.collection.mutable.ListBuffer[Int]()
    def randomPort: Int = {
        val port = util.Random.nextInt(60000) + 2000
        if(usedPorts.contains(port)) {
            randomPort
        } else {
            usedPorts += port
            port
        }
    }

    def currentAccount = SvdAccount(uid = randomPort, userName = System.getProperty("user.name"))

}
