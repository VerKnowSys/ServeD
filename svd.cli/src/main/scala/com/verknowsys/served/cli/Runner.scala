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
    private lazy val helpContent = io.Source.fromURL(Thread.currentThread.getContextClassLoader.getResource("help.txt")).mkString
    
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

// 
//     
//             
//         
//         svd ! Connect
//         receiveWithin(3000) {
//             case Success => success("Connected")
//             case Error => 
//                 error("Connection error")
//                 exit
//             case TIMEOUT =>
//                 error("Connection timeout")
//                 exit
//         }
//         
//         println("ServeD 0.1.0 interactive shell. Welcome!")
//         // Timeout.start
//         while(true){
//             val msg = Console.readLine(">>> ")
//             // Timeout.reset
//             if(msg == "exit") System.exit(0)
//             val args = msg.split(" ").toList
//             println(args)
//             if(!args.isEmpty) process(args)
//         }
//         
//         def success(name: String){
//             // TODO: Colorize
//             println("[OK] " + name)
//         }
//         
//         def error(name: String){
//             // TODO: Colorize
//             println("[ERROR] " + name)
//         }
//        
//         def process(params: List[String]) {
//             params match {
//                 case "git" :: tail => tail match {
//                 
//                     case "create" :: name :: Nil =>
//                         svd !? Git.CreateRepository(name) match {
//                             case Success => success("Git repository %s created successfully.".format(name))
//                             case Git.RepositoryExistsError => error("Git repository %s already exists".format(name))
//                         }
//                     
//                     case "remove" :: name :: Nil =>
//                         svd !? Git.RemoveRepository(name) match {
//                             case Success => success("Git repository %s removed successfully".format(name))
//                         }
//                     
//                     case "list" :: Nil =>
//                         // svd ! Git.ListRepositories match {
//                             // case Git.Repositories(list) =>
//                                 // list.foreach { repo =>
//                                     // println(repo)
//                                 // }
//                         // }
//                         
//                         (svd !! Git.ListRepositories).inputChannel.receiveWithin(4000) {
//                             case Git.Repositories(list) => println(list)
//                             case TIMEOUT => println("sorry, timeout")
//                         }
//                         
//                     
//                     case _ => println("TODO: git help")
//                 }
//             
//                 case "help" :: Nil => help
//                 
//                 case _ => help
//             }
//         }
//         
//         def help {

// 
// // TODO: Read this from file
//         }
//     }

// object Timeout {
//     class TimeoutThread extends Thread {
//         var keep = true
//         var left = time
//         override def run {
//             while(keep && left > 0){
//                 Thread.sleep(100)
//                 left -= 100
//             }
//             
//             if(keep){
//                println("== Timeout ==")
//                 System.exit(1)   
//             }
//         }
//         def kill { keep = false }
//     }
//     
//     final private val time = 20 * 1000 // 20 seconds
//     private var thread: Option[TimeoutThread] = None
//         
//     def start {
//         thread = Some(new TimeoutThread)
//         thread.foreach(_.start)
//     }
//     
//     def stop {
//         thread.foreach(_.kill)
//     }
//     
//     def reset {
//         stop
//         start
//     }
// }
