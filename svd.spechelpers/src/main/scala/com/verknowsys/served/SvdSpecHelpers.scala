package com.verknowsys.served

import org.apache.commons.io.FileUtils
import java.io.File
import scala.actors.Actor
import scala.collection.mutable.ListBuffer

object SvdSpecHelpers {
	implicit def StringToFile(s: String) = new File(s)
    implicit def ItemToSeq[T](a: T) = a :: Nil
    
    def touch(file: File) = FileUtils.touch(file)
    
    def readFile(path: String) = FileUtils.readFileToString(path)

    def writeFile(path: String, data: String) = FileUtils.writeStringToFile(path, data)
    
    def mkdir(path: String) = FileUtils.forceMkdir(path)
    
    def rmdir(path: String) = try { FileUtils.forceDelete(path) } catch { case _ => }
    
    final val TEST_DIR = "/tmp/served"
    var count = 0
    def randomPath = {
        count += 1
        TEST_DIR + "/dir_" + count + "_" + System.currentTimeMillis
    }
    
    def testPath(path: String) = TEST_DIR + "/" + path
    
    def removeTestDir = rmdir(TEST_DIR)
    def createTestDir = mkdir(TEST_DIR)
    def reloadTestDir = {
        removeTestDir
        createTestDir
    }
    
    def restoreFile(path: String)(f: => Unit){
        val content = readFile(path)
        f
        writeFile(path, content)
    }
    
    @deprecated("Use SvdExpectActor")
    def waitWhileRunning(actors: Seq[Actor]*) {
        actors.flatten foreach { a =>
            while(a.getState != Actor.State.Blocked && a.getState != Actor.State.Suspended) { 
                println("Actor state: " + a.getState)
                // if(a.getState == Actor.State.Terminated) println("[WARNING !!!] Actor Terminated - was supposed to be running - " + a)
                waitFor(50)
            }
        }
    }
    
    @deprecated("Use SvdExpectActor")
    def waitForDeath(actors: Seq[Actor]*) {
        actors.flatten foreach { a =>
            while(a.getState != Actor.State.Terminated) { 
                waitFor(50)
            }
        }
    }
    
    def waitFor(time: Int) = Thread.sleep(time)
    
    // def waitForEnter {
    //     println("Press enter to continue...")
    //     System.in.read
    // }
    
    @deprecated("Use SvdExpectActor")
    class Counter extends Actor {
        case object Stop
        
        val data = new ListBuffer[Any]
        start
        
        def act {
            loop {
                receive {
                    case Stop => exit
                    case x: Any => data += x
                }
            }
        }
        
        def stop { this ! Stop }
    }
}