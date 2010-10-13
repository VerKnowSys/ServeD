package com.verknowsys.served.maintainer

import scala.actors.Actor
import scala.io.Source

import com.verknowsys.served.Config
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.notifications.SvdGitNotifier

case class GetUsers(val list: List[Account])
case class Message(val value: String)

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
    
    def main(args: Array[String]): Unit = {
        accounts.find (_.userName == "teamon") map { a =>
            println(a.repositories.toList)
            a.createRepository("xxx")
        }
    }


}
