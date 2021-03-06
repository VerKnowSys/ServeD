/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served

// import com.verknowsys.served.utils.LoggerUtils
// import com.verknowsys.served.utils.Logging
// import com.verknowsys.served.api.Logger

import org.scalatest._
import org.scalatest.matchers._
// import akka.testkit.TestKit
import java.io.File
import scala.io.Source
import org.apache.commons.io.FileUtils
import com.verknowsys.served.api.SvdAccount


package object testing {


    // import scala.concurrent._
    // import akka.pattern.ask
    // import scala.concurrent.Duration
    // import akka.util.Timeout
    // import scala.concurrent.duration._
    // import akka.actor._


    trait TestLogger {
        // LoggerUtils.addEntry("com.verknowsys.served", Logger.Levels.Warn)
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
                         // with TestKit
                        with TestLogger
                        with OneInstancePerTest
                        with BeforeAndAfterEach
                        with CustomMatchers
                        with MustMatchers
                        with BeforeAndAfterAll
                         // with Logging


    // Common types and objects mapping
    type Actor = akka.actor.Actor
    val Actor = akka.actor.Actor
    type ActorRef = akka.actor.ActorRef

    // Utility methods
    def testPublicKey = Source.fromURL(getClass.getResource("/test_key_rsa.pub")).getLines.mkString("\n")

    implicit def StringToFile(s: String) = new File(s)

    def tmpDir = "/var/tmp"

    def fssecure(path: String)(f: String => Unit) { if(path.startsWith(tmpDir)) f(path) }

    def touch(path: String) = fssecure(path) { p => FileUtils touch p }

    def readFile(path: String) = fssecure(path) { p => FileUtils readFileToString p }

    def writeFile(path: String, data: String) = fssecure(path) { p => FileUtils.writeStringToFile(p, data) }

    def mkdir(path: String) = fssecure(path) { p => FileUtils forceMkdir p }

    def rmdir(path: String) = fssecure(path) { p => try { FileUtils forceDelete p } catch { case _ => } }

    final val TEST_DIR = tmpDir + "/served"
    private var count = 0
    def randomPath = {
        count += 1
        val dir = TEST_DIR + "/dir_" + count + "_" + java.util.UUID.randomUUID.toString
        mkdir(dir)
        dir
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
        writeFile(path, content.toString)
    }

    def waitFor(time: Int) = Thread.sleep(time)


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
