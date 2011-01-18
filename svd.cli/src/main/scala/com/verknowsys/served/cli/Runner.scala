package com.verknowsys.served.cli

import akka.actor.Actor
import com.verknowsys.served.api._

/** 
 * CLI 
 * 
 * @param ServeD instance host
 * @param ServeD instance port
 * @author teamon
 */
class ApiClient(host: String, port: Int){
    val svd = Actor.remote.actorFor("service:api", host, port)
    
    println("Checking connection...")
    (svd !! General.Connect(username)) match {
        case Some(response) => response match {
            case Success => 
                println("ServeD 0.1.0 interactive shell. Welcome!")
                prompt
            case Error(message) =>
                println("[ERROR] " + message)
                exit
        }
        case None => 
            println("[ERROR] Connection timeout")
    }
    

    /**
     * Show prompt and read arguments
     *
     * @author teamon
     */
    def prompt {
        val args = Console.readLine(">>> ").split(" ").filterNot(_ == "")
        if(!args.isEmpty) process(args)
        prompt
    }
    
    
    /**
     * Match parameters to correct action
     *
     * @author teamon
     */
    def process(args: Seq[String]){
        args match {
            case "exit" :: Nil => exit
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
    private val username = System.getProperty("user.name")

    /**
     * Read help file
     * 
     * @author teamon
     */
    private lazy val helpContent = io.Source.fromURL(Thread.currentThread.getContextClassLoader.getResource("help.txt")).mkString
}


object Runner {
    def main(args: Array[String]): Unit = {
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
