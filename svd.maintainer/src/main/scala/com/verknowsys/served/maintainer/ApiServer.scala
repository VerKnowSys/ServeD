package com.verknowsys.served.maintainer

import scala.actors.Actor
import scala.actors.Actor._
import scala.actors.remote.RemoteActor
import scala.actors.remote.RemoteActor._
import scala.actors.remote.Node

import com.verknowsys.served.utils._
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.api._

object ApiServer extends CommonActor {
    final val port = 5555 // XXX: Hardcoded port number

    start

    def act {
        loop {
            react {
                case Init =>
                    RemoteActor.classLoader = getClass().getClassLoader()
                    alive(port)
                    register('ServeD, self)
                    info("Started API Server at " + port)

                case Quit =>
                    info("Quitting ApiServer")
                    exit
                
                
                case Connect =>
                    trace("Connect from %" % sender)
                    sender ! Success
                
                case msg: ApiMessage => 
                    trace("Got ApiMessage: % fromm %" % (msg, sender.receiver)) 
                
                case x => messageNotRecognized(x)
            }
        }
    }
}
