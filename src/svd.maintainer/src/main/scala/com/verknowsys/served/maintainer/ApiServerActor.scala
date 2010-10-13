package com.verknowsys.served.maintainer

import scala.actors.Actor
import scala.actors.Actor._
import scala.actors.remote.RemoteActor
import scala.actors.remote.RemoteActor._
import scala.actors.remote.Node

import com.verknowsys.served.api._
import com.verknowsys.served.utils.Utils

object ApiServerActor extends Actor with Utils {
    final val port = 5555 // XXX: Hardcoded port number

    RemoteActor.classLoader = getClass().getClassLoader()

    start
    
    def act {
        alive(port)
        register('ServeD, self)
        
        Actor.loop {
            receive {
                case Git.CreateRepository(name) => 
                    logger.trace("Created git repository: " + name)
                    sender ! Git.RepositoryExistsError
                
                case Git.RemoveRepository(name) =>
                    logger.trace("Removed git repository: " + name)
                    sender ! Success
                    
                case Git.ListRepositories =>
                    logger.trace("List repositories")
                    sender ! Git.Repositories(List(
                        Git.Repository("first"),
                        Git.Repository("second")
                    ))
            }
        }
    }
}