package com.verknowsys.served.cli

import com.verknowsys.served.api.Admin

abstract class Commands {
    def commands(implicit svd: Svd): PartialFunction[Args, Unit]
}

object GeneralCommands extends Commands {
    def commands(implicit svd: Svd) = {
        case "info" :: Nil =>
            request(Admin.ListTreeActors) {
                case Admin.ActorsList(arr) => printActorsTree(arr.toList)
            }

        case "echo" :: xs => 
            svd ! xs.mkString(" ")
            
        case "exit" :: Nil => 
            quit
            
        case "help" :: Nil => 
            displayHelp
            
        case _ => 
            displayHelp
    }
    
    def displayHelp = println(helpContent)
    
    
    def printActorsTree(actors: List[Admin.ActorInfo], level: Int = 0){
        val size = 60 - 2*level
        actors foreach { a =>
            
            info("%s[%-"+size+"s] %s (%s)", "  " * level, shortClassName(a.className), a.uuid, a.status)
            printActorsTree(a.linkedActors, level + 1)
        }
    }
    
    def shortClassName(className: String) = className.replace("com.verknowsys.served.", "")
    
    /**
     * Read help file
     * 
     * @author teamon
     */
    private lazy val helpContent = io.Source.fromURL(Thread.currentThread.getContextClassLoader.getResource("svd.ronn")).mkString
}
