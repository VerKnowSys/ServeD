package com.verknowsys.served.cli

import com.verknowsys.served.api._
import scala.actors.Actor
import scala.actors.remote.RemoteActor
import scala.actors.remote.RemoteActor._
import scala.actors.remote.Node

class ApiClientActor(host: String, port: Int, args: List[String]) extends Actor {
    // make use of http://github.com/rtomayko/ronn

    final val HELP = Map(
        "" -> """
svd command line tool help

== git ==
git create [name]   Create repository
git remove [name]   Remove repository
git list all        List all repositories   

== other ==
help                Display help
help [command]      Display help for command, e.g. 'help git create'
exit                Quit interactive console 

""",
        "git create" -> "Help for git create"
    )

    start
    
    def act {
        RemoteActor.classLoader = getClass().getClassLoader()
        val svd = select(Node(host, port), 'ServeD)
        link(svd)

        if(args.isEmpty){
            println("ServeD 0.1.0 interactive shell. Welcome!")
            while(true){
                val msg = System.console.readLine(">>> ")
                if(msg == "exit") System.exit(0)
                val ar = msg.split(" ").toList
                if(!ar.isEmpty) process(ar)
            }
        } else {
            process(args)
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
                        svd !? Git.ListRepositories match {
                            case Git.Repositories(list) =>
                                list.foreach { repo =>
                                    println(repo)
                                }
                        }
                        
                    case _ => println("TODO: git help")
                }
                
                case "help" :: Nil => println(HELP)
            }
        }
    }
}


object Runner {
    def main(args: Array[String]): Unit = {
        new ApiClientActor("127.0.0.1", 5555, args.toList.filterNot { _.startsWith("-") }) // XXX: Hardcoded host and port
    }
}