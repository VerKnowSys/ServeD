package com.verknowsys.served.managers

import scala.actors.Actor
import scala.actors.Actor._
import scala.actors.remote.RemoteActor
import scala.actors.remote.RemoteActor._
import scala.actors.remote.Node

import com.verknowsys.served.api._

class ApiManager(owner: AccountManager) extends Manager(owner) {
    final val port = 5555 // XXX: Hardcoded port number

    RemoteActor.classLoader = getClass().getClassLoader()
    
    def act {
        alive(port)
        register('ServeD, self)
        
        loop {
            receive {
                // case msg: ApiMessage => sender ! (owner !! msg) 
                
                case _ => messageNotRecognized(_)
            }
        }
    }
}
