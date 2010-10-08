package com.verknowsys.served.cli

import com.verknowsys.served.api._
import scala.actors.Actor
import scala.actors.remote.RemoteActor
import scala.actors.remote.RemoteActor._
import scala.actors.remote.Node

class ApiClientActor(host: String, port: Int, args: List[String]) extends Actor {
    // make use of http://github.com/rtomayko/ronn
    // 
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


    val peer = Node(host, port)

    start
    
    def act {
        RemoteActor.classLoader = getClass().getClassLoader()
        val svd = select(peer, 'ServeD)
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
       
        def process(params: List[String]) {
            val res = params match {
                case "git" :: "create" :: name :: Nil => svd !! CreateGitRepository(name)
                case "git" :: "remove" :: name :: Nil => svd !! RemoveGitRepository(name)
                case "git" :: "list" :: "all" :: Nil =>  svd !! RemoveGitRepository("name")
              
                case "help" :: commands => 
                    HELP.get(commands.mkString(" ")) match {
                        case Some(help) => () => Notice(help)
                        case None => () => Error("Help not found")
                    }
            
                case _ => () => Error("Command not found. Type 'help' for help.")
            }
            
            res() match {
                case Success(msg) => println("[OK] " + msg)
                case Notice(msg) => println(msg)
                case Error(msg) => println("[ERROR] " + msg)
                case _ => println("*** WRONG!! ***")
            }
        }
    }
}


object Runner {
    def main(args: Array[String]): Unit = {
        new ApiClientActor("127.0.0.1", 5555, args.toList.filterNot { _.startsWith("-") }) // XXX: Hardcoded host and port
    }
}