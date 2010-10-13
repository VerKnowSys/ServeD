// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.maintainer

/**
 * User: dmilith
 * Date: Dec 12, 2009
 * Time: 1:34:27 AM
 */

import com.verknowsys.served._
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.git._
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.notifications._

import scala.collection.mutable.ListBuffer
import scala.actors.Actor
import scala.io.Source
import java.io._
import java.nio.charset.Charset
import org.apache.commons.io._
import org.apache.log4j.{Level, Logger}


case class GetUsers(val list: List[Account])
case class Message(val value: String)


case class Account(
        val userName: String = "guest",
        val pass: String = "x",
        val uid: String = "1000",
        val gid: String = "1000",
        val information: String = "No information",
        val homeDir: String = "/home/",
        val shell: String = "/bin/bash"
        ) extends Utils {
            
    def this(a: List[String]) = this(
        userName = a(0),
        pass = a(1),
        uid = a(2),
        gid = a(3),
        information = a(4),
        homeDir = a(5),
        shell = a(6)
    )
    
    lazy val repositories = loadRepositories
    
    // def this(a: List[String]) = this(a(0), a(1), a(2), a(3), a(4), a(5), a(6))

    def size = {
        try {
            val elementsSize = FileUtils.sizeOfDirectory(new File(homeDir))
            logger.debug("getAccountSize of " + homeDir + " folder: " + (elementsSize))
            Some(elementsSize)
        } catch {
            case x: Exception =>
                logger.error("Error: " + x)
                None
        }
    }
    
    protected def loadRepositories = {
        List(1,2)
    }

}


object SvdAccountManager extends Actor with Utils {
    
    start
    
    lazy val accounts = loadAccounts
    
    
    def act {
        logger.trace("Java Library Path Property: " + System.getProperty("java.library.path"))

        def matchIt(name: String) = name match {
            case Config.passwdFileName =>
                logger.trace("Triggered (modified/created) system password file: %s".format(Config.passwdFileName))
                SvdMaintainer ! Message("Modified or Created system passwd file") // XXX: Sending Message(msg) doesnt make any sense -> use custom case class

            case _ =>
                logger.trace("No trigger on file")
        }
        
        val gitNotifier = new SvdGitNotifier(Config.defaultGitRepoToWatch)

        val watchEtc = new FileWatcher(Config.etcPath, recursive = false) {
            override def created(name: String) {
                logger.trace("File created: " + name)
                matchIt(name)
            }

            override def modified(name: String) {
                logger.trace("File modified: " + name)
                matchIt(name)
                // SvdMaintainer ! Message("Modified file: " + name)
            }

            override def deleted(name: String) {
                logger.trace("File deleted: " + name)
            }
        }

        Actor.loop {
            receive {
                case Init =>
                    logger.debug("AccountManager ready for tasks")
                    logger.debug("Initialized watch for " + Config.etcPath)
                    logger.trace("WatchEtc: " + watchEtc)
                    gitNotifier.start
                    gitNotifier ! Init
                    logger.info("AccountManager ready")
                    
                case Quit =>
                    logger.info("Quitting AccountManager…")
                    watchEtc.stop
                    gitNotifier ! Quit
                    
                // case GetUsers =>
                    // logger.debug("Sending Users… ")
                    // SvdMaintainer ! GetUsers(getUsers)
                // getAccountSize("_carddav") // XXX: hardcoded for test
                // getAccountSize("nonExistantOne") // XXX: hardcoded for test
                
                case x: AnyRef =>
                    logger.warn("Command not recognized. AccountManager will ignore You: " + x.toString)
                    
            }
        }
    }





    /**
     * @author dmilith
     *
     * Function to parse and convert List[String] of passwd file entries to List[Account]
     *
     */
    def loadAccounts: List[Account] = {
        val rawData = Source.fromFile(Config.systemPasswdFile, "utf-8").getLines.toList
        for(line <- rawData if !line.startsWith("#")) // XXX: hardcode
            yield
                new Account(line.split(":").toList)
    }

}
