package com.verknowsys.served.cli

import akka.actor.{Actor, ActorRef}
import com.verknowsys.served.api.{Success, Error, General, RemoteSession}


/** 
 * CLI 
 * 
 * @author teamon
 */
class ApiClient(svd: Svd) {
    implicit val served = svd
    
    val commands = (
        LoggerCommands ::
        GitCommands ::
        GeneralCommands ::
        Nil
    ).map(_.commands).reduce(_ orElse _)

    // Connect to ServeD
    request(General.Connect(username)) {
        case Success => 
            info("ServeD interactive shell. Welcome %s", username)
            prompt(new Prompt)
        case Error(message) =>
            error(message)
            quit
    }
    

    
    /**
     * Show prompt and read arguments
     *
     * @author teamon
     */
    def prompt(in: Prompt) {
        val args = in.readLine
        if(!args.isEmpty) commands(args.toList)
        prompt(in)
    }

    /**
     * Get system username
     *
     * @author teamon
     */ 
    private lazy val username = System.getProperty("user.name")
}

object Runner {
    def main(args: Array[String]) {
        if(args.length == 2) {
            RemoteSession(args(0), args(1).toInt) match {
                case Some(svd) => new ApiClient(svd)
                case None => error("Unable to connect to ServeD")
            }
        } else { 
            error("Usage: com.verknowsys.served.cli.Runner HOST PORT")
        }
    }
}
