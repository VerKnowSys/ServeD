package com.verknowsys.served.cli

import com.verknowsys.served.api.Admin

abstract class Commands {
    def commands(implicit svd: Svd): PartialFunction[Args, Unit]
}

object GeneralCommands extends Commands {
    def commands(implicit svd: Svd) = {
        case "info" :: Nil =>
            request(Admin.ListActors) {
                case Admin.ActorsList(list) => print(list)
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
    
    /**
     * Read help file
     * 
     * @author teamon
     */
    private lazy val helpContent = io.Source.fromURL(Thread.currentThread.getContextClassLoader.getResource("svd.ronn")).mkString
}
