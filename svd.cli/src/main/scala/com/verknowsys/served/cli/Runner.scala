package com.verknowsys.served.cli

import akka.actor.Actor
import akka.util.Logging
import com.verknowsys.served.api._

/** 
 * CLI 
 * 
 * @param ServeD instance host
 * @param ServeD instance port
 * @author teamon
 */
class ApiClient(host: String, port: Int) extends Logging {
    val svd = Actor.remote.actorFor("service:api", host, port)

    log.trace("Checking connection...")

    request(General.Connect(username)) {
        case Success => 
            println("ServeD 0.1.0 interactive shell. Welcome %s".format(username))
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
                        case Git.Repositories(list) => list.foreach(r => println(" - " + r))
                    }
                    
                case "create" :: name :: Nil =>
                    request(Git.CreateRepository(name)) {
                        case Success =>
                            log.info("Repository %s created", name)
                        case Git.RepositoryExistsError => 
                            log.error("Repository with name %s already exists", name)
                    }
                    
                case ("remove" | "rm") :: name :: Nil =>
                    // TODO: Confirm!
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
            
            
            case "echo" :: xs => svd ! xs.mkString(" ")
            case "exit" :: Nil => quit
            case "help" :: Nil => displayHelp
            case _ => displayHelp
        }
    }
    
    def displayHelp = println(helpContent)  
    
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
        if(args.length == 2) new ApiClient(args(0), args(1).toInt)
        else { println("Usage: com.verknowsys.served.cli.Runner HOST PORT")}
    }
}
