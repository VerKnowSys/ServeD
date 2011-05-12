package com.verknowsys.served.cli

import akka.actor.{Actor, ActorRef}
import com.verknowsys.served.utils.Logging
import com.verknowsys.served.api._


/** 
 * CLI 
 * 
 * @author teamon
 */
class ApiClient(svd: ActorRef) extends Logging {
    request(General.Connect(username)) {
        case Success => 
            println("ServeD interactive shell. Welcome %s".format(username))
            prompt
        case Error(message) =>
            log.error("[ERROR] " + message)
            quit
    }

    /**
     * Show prompt and read arguments
     *
     * @author teamon
     */
    def prompt {
        val args = Console.readLine(">>> ").split(" ").filterNot(_ == "")
        if(!args.isEmpty) process(args.toList)
        prompt
    }
    
    def request(msg: ApiMessage)(f: PartialFunction[Any, Unit]) {
        (svd !! msg) match {
            case Some(response) if f.isDefinedAt(response) => f(response)
            case Some(response) => log.error("Unhandled reponse %s", response)
            case None => log.error("Connection timeout")
        }
    }
    
    def confirm(msg: String) = {
        log.warn(msg)
        Console.readLine("[Y/n]: ") == "Y"
    }
    
    
    /**
     * Match parameters to correct action
     *
     * @author teamon
     */
    def process(args: List[String]){
        log.debug("args: %s", args)
        args match {
            
            case "git" :: xs => xs match {
                case "list" :: Nil => 
                    request(Git.ListRepositories) {
                        case Git.Repositories(list) => print(list)
                    }
                    
                case "create" :: name :: Nil =>
                    request(Git.CreateRepository(name)) {
                        case Success =>
                            log.info("Repository %s created", name)
                        case Git.RepositoryExistsError => 
                            log.error("Repository with name %s already exists", name)
                    }
                    
                case ("remove" | "rm") :: name :: Nil =>
                    if(confirm("Are you sure you want to remove repository %s? This operation cannot be undone!".format(name))){
                        request(Git.RemoveRepository(name)) {
                            case Success =>
                                log.info("Repository %s removed", name)
                            case Git.RepositoryDoesNotExistError =>
                                log.error("Repository with name %s does not exist", name)
                        }
                    }
                case _ => log.error("Command not found. TODO: Display help for git commands")
            }
            
            case "info" :: Nil =>
                request(Admin.ListActors) {
                    case Admin.ActorsList(list) => list.foreach(println)
                }
                
            case "logger" :: xs => xs match {
                case "list" :: Nil =>
                    request(Logger.ListEntries) {
                        case Logger.Entries(entries) => print(entries.map(e => e._1 + ": " + e._2))
                    }
                    
                case ("remove" | "rm") :: className :: Nil =>
                    request(Logger.RemoveEntry(className)){
                        case Success => log.info("Entry removed")
                    }
                
                case className :: level :: Nil => 
                    parseLoggerLevel(level) match {
                        case Some(lvl) =>
                            request(Logger.AddEntry(className, lvl)){
                                case Success => log.info("Entry added")
                            }
                            
                        case None =>
                            log.error("Invalid logger level")
                    }
                    
                
                
                case _ => log.error("logger [list|add|remove]")
                
            }
            
            
            case "echo" :: xs => svd ! xs.mkString(" ")
            case "exit" :: Nil => quit
            case "help" :: Nil => displayHelp
            case _ => displayHelp
        }
    }
    
    def print(list: Iterable[String]) = list.foreach(println)
    
    def displayHelp = println(helpContent)  
    
    def parseLoggerLevel(str: String) = str.toLowerCase match {
        case "error" => Some(Logger.Levels.Error)
        case "warn"  => Some(Logger.Levels.Warn)
        case "info"  => Some(Logger.Levels.Info)
        case "debug" => Some(Logger.Levels.Debug)
        case "trace" => Some(Logger.Levels.Trace)
        case _ => None
    }
    
    /**
     * Get system username
     *
     * @author teamon
     */ 
    private lazy val username = System.getProperty("user.name")

    /**
     * Read help file
     * 
     * @author teamon
     */
    private lazy val helpContent = io.Source.fromURL(Thread.currentThread.getContextClassLoader.getResource("svd.ronn")).mkString
    
    private def quit {
        Actor.registry.shutdownAll
        System.exit(0)
    }
}


object Runner extends Logging {
    def main(args: Array[String]) {
        if(args.length == 2) {
            RemoteSession(args(0), args(1).toInt) match {
                case Some(svd) => new ApiClient(svd)
                case None => log.error("Unable to connect to ServeD")
            }
        } else { 
            log.error("Usage: com.verknowsys.served.cli.Runner HOST PORT")
        }
    }
}
