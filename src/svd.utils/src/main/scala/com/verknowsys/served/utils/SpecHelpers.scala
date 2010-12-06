package com.verknowsys.served.utils

import org.apache.commons.io.FileUtils
import java.io.File
import scala.actors.Actor

object SpecHelpers extends Utils {
    implicit def StringToFile(s: String) = new File(s)
    
    def waitFor(actor: Actor) {
        println("[spec] Waiting for " + actor)
        var time = 0
        while(actor.getState != Actor.State.Blocked) { time+=1; Thread.sleep(50) }
        println("[spec] Waited " + time*50 + "ms")
    }

    def waitFor(time: Int) {
        println("[spec] Waiting for " + time + "ms")
        Thread.sleep(time)
    }
    
    def readFile(path: String) = FileUtils.readFileToString(path)
    
    def writeFile(path: String, data: String) = FileUtils.writeStringToFile(path, data)
}

