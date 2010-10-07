package com.verknowsys.served.cli

import com.verknowsys.served.api._
import scala.actors.Actor
import scala.actors.remote.RemoteActor
import scala.actors.remote.RemoteActor._
import scala.actors.remote.Node

class ApiClientActor(host: String, port: Int, args: List[String]) extends Actor {
    final val HELP = """
svd command line tool help

== git ==
git create [name]   Create repository
git remove [name]   Remove repository
git list all        List all repositories   

== other ==
help                Display help
exit                Quit interactive console 

"""
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
                case "git" :: "create" :: name :: Nil =>
                    svd !! CreateGitRepository(name)
                    
                case "git" :: "remove" :: name :: Nil =>
                    svd !! RemoveGitRepository(name)
                    
                case "git" :: "list" :: "all" :: Nil => 
                    svd !! RemoveGitRepository("name")
       
                case "help" :: Nil => () => println(HELP)
       
                case _ => () => Error("Type 'help' for help.")
            }
            
            res()
        }
       
        def error { println("[ERROR] Command not found. Type 'help' for help") }
    }
}


object Runner {
    def main(args: Array[String]): Unit = {
        new ApiClientActor("127.0.0.1", 5555, args.toList.filterNot { _.startsWith("-") }) // XXX: Hardcoded host and port
    }
}