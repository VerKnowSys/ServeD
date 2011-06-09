package com.verknowsys.served

import org.apache.commons.io.FileUtils
import java.io.File
import akka.actor.ActorRef
import com.verknowsys.served.systemmanager.native.SvdAccount
import scala.io.Source


object SvdSpecHelpers {
	implicit def StringToFile(s: String) = new File(s)
    implicit def ItemToSeq[T](a: T) = a :: Nil
    
    def touch(file: File) = {
        if (file.startsWith("/var/tmp"))
            FileUtils.touch(file)
    }
    
    def readFile(path: String) = {
        if (path.startsWith("/var/tmp"))
            FileUtils.readFileToString(path)
    }

    def writeFile(path: String, data: String) = {
        if (path.startsWith("/var/tmp"))
            FileUtils.writeStringToFile(path, data)
    }
    
    def mkdir(path: String) = {
        if (path.startsWith("/var/tmp"))
            FileUtils.forceMkdir(path)
    }
    
    def rmdir(path: String) = try {
        if (path.startsWith("/var/tmp"))
            FileUtils.forceDelete(path)
    } catch { case _ => }
    
    final val TEST_DIR = "/var/tmp/served"
    var count = 0
    def randomPath = {
        count += 1
        TEST_DIR / "/dir_" + count + "_" + System.currentTimeMillis
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
        writeFile(path, content)
    }

    def waitFor(time: Int) = Thread.sleep(time)
    
    def currentAccount = {
        val rawData = Source.fromFile(SvdConfig.systemPasswdFile, SvdConfig.defaultEncoding).getLines.toList
        val accounts: List[SvdAccount] = rawData collect {
            case SvdAccount(account) => account
        }
        accounts.find(_.userName == System.getProperty("user.name")).get
    }

}