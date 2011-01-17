package com.verknowsys.served.cli

import com.verknowsys.served.api._
import scala.actors.{Actor, TIMEOUT}
import scala.actors.remote.RemoteActor
import scala.actors.remote.RemoteActor._
import scala.actors.remote.Node

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

class ApiClientActor(host: String, port: Int) extends Actor {
    // make use of http://github.com/rtomayko/ronn for man()

    start
        
    def act {
        RemoteActor.classLoader = getClass().getClassLoader()
        val svd = select(Node(host, port), 'ServeD)
        link(svd)
        
        println("Checking connection...")
        
        svd ! Connect
        receiveWithin(3000) {
            case Success => success("Connected")
            case Error => 
                error("Connection error")
                exit
            case TIMEOUT =>
                error("Connection timeout")
                exit
        }
        
        println("ServeD 0.1.0 interactive shell. Welcome!")
        // Timeout.start
        while(true){
            val msg = Console.readLine(">>> ")
            // Timeout.reset
            if(msg == "exit") System.exit(0)
            val args = msg.split(" ").toList
            println(args)
            if(!args.isEmpty) process(args)
        }
        
        def success(name: String){
            // TODO: Colorize
            println("[OK] " + name)
        }
        
        def error(name: String){
            // TODO: Colorize
            println("[ERROR] " + name)
        }
       
        def process(params: List[String]) {
            params match {
                case "git" :: tail => tail match {
                
                    case "create" :: name :: Nil =>
                        svd !? Git.CreateRepository(name) match {
                            case Success => success("Git repository %s created successfully.".format(name))
                            case Git.RepositoryExistsError => error("Git repository %s already exists".format(name))
                        }
                    
                    case "remove" :: name :: Nil =>
                        svd !? Git.RemoveRepository(name) match {
                            case Success => success("Git repository %s removed successfully".format(name))
                        }
                    
                    case "list" :: Nil =>
                        // svd ! Git.ListRepositories match {
                            // case Git.Repositories(list) =>
                                // list.foreach { repo =>
                                    // println(repo)
                                // }
                        // }
                        
                        (svd !! Git.ListRepositories).inputChannel.receiveWithin(4000) {
                            case Git.Repositories(list) => println(list)
                            case TIMEOUT => println("sorry, timeout")
                        }
                        
                    
                    case _ => println("TODO: git help")
                }
            
                case "help" :: Nil => help
                
                case _ => help
            }
        }
        
        def help {
println("""
svd command line tool help

== git ==
git create [name]   Create repository
git remove [name]   Remove repository
git list all        List all repositories   

== other ==
help                Display help
help [command]      Display help for command, e.g. 'help git create'
exit                Quit interactive console 

""")

// TODO: Read this from file
        }
    }
}


object Runner {
    def main(args: Array[String]): Unit = {
        if(args.length == 2) new ApiClientActor(args(0), args(1).toInt)
        else { println("Usage: com.verknowsys.served.cli.Runner HOST PORT")}
    }
}