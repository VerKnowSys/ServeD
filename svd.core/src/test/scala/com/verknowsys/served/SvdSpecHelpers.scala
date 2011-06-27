package com.verknowsys.served

import com.verknowsys.served.utils._
import com.verknowsys.served._

import org.apache.commons.io.FileUtils
import java.io.File
import akka.actor.ActorRef
import com.verknowsys.served.api.SvdAccount
import scala.io.Source


object SvdSpecHelpers {
	implicit def StringToFile(s: String) = new File(s)
    implicit def ItemToSeq[T](a: T) = a :: Nil
    
    def touch(file: File) = {
        if (file.startsWith(SvdConfig.systemTmpDir))
            FileUtils.touch(file)
    }
    
    def readFile(path: String) = {
        if (path.startsWith(SvdConfig.systemTmpDir))
            FileUtils.readFileToString(path)
    }

    def writeFile(path: String, data: String) = {
        if (path.startsWith(SvdConfig.systemTmpDir))
            FileUtils.writeStringToFile(path, data)
    }
    
    def mkdir(path: String) = {
        if (path.startsWith(SvdConfig.systemTmpDir))
            FileUtils.forceMkdir(path)
    }
    
    def rmdir(path: String) = try {
        if (path.startsWith(SvdConfig.systemTmpDir))
            FileUtils.forceDelete(path)
    } catch { case _ => }
    
    final val TEST_DIR = SvdConfig.systemTmpDir / "served"
    var count = 0
    def randomPath = {
        count += 1
        val dir = TEST_DIR / "dir_" + count + "_" + java.util.UUID.randomUUID.toString
        mkdir(dir)
        dir
    }
    
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
    
    def testPath(path: String) = TEST_DIR / path
    
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
    
    def currentAccount = {
        val accounts: List[SvdAccount] =
            SvdAccount(uid = randomPort, gid = randomPort, dbPort = randomPort, servicePort = randomPort, userName = "żółć") ::
            SvdAccount(uid = randomPort, gid = randomPort, dbPort = randomPort, servicePort = randomPort, userName = System.getProperty("user.name")) ::
            SvdAccount(uid = randomPort, gid = randomPort, dbPort = randomPort, servicePort = randomPort, userName = "gęś") :: Nil
        val account = accounts.find(_.userName == System.getProperty("user.name")).get
        // account.copy(homeDir = randomPath)
        account
    }

}
