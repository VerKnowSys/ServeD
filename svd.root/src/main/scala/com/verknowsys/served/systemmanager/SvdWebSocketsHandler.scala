// © Copyright 2009 Daniel Dettlaff, Tymon Tobolski. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.systemmanager


import com.verknowsys.served._
import com.verknowsys.served.utils._
import org.webbitserver._
import org.webbitserver.handler._


trait DQueue[T] {
    import scala.collection.mutable.Queue

    val current = new Queue[T]
    val temp = new Queue[T]
    val history = new Queue[T]


    def push(e: T) {
        if (current.length >= N) {
            temp enqueue current.dequeue
            if(temp.length >= K){
                history enqueue average(temp.toSeq)
                temp.clear
            }
        }
        current enqueue e
    }


    /**
     *    @author teamon, dmilith
     *
     *   N - Value describing amount of elements to keep in defined queue
     */
    def N: Int


    /**
     *    @author teamon, dmilith
     *
     *   K - Value describing amount of elements gathered after which we need to perform normalization / standardisation
     */
    def K: Int


    def average(elems: Seq[T]): T

}


// class IntQ extends DQueue[Int] {
//
//     def N = 10
//
//     def K = 3
//
//     def average(items: Seq[Int]) = items.reduceLeft(_ + _) / K
//
// }
//

case class SvdProcessUsage(
    val pid: Int,
    val name: String,
    val rss: Int
    )


class SvdProcessQueue extends DQueue[SvdProcessUsage] {


    def N = 60 * 15 // 15 minutes data from each second to keep in queue


    def K = 60 // standarisation/ normalization after each 60 seconds


    def average(elems: Seq[SvdProcessUsage]) = {
        // elems.reduceLeft(SvdProcessUsage(pid = _.pid, name = _.name, rss = _.rss + _.rss)) / K // XXX: hack - for resident set size now
        SvdProcessUsage(name = "SSS", pid = 666, rss = 0)
    }


}


class SvdWebSocketsHandler extends WebSocketHandler with Logging with SvdUtils {

    import com.verknowsys.served.systemmanager.native._

    var connectionCount = 0
    var queue: SvdProcessQueue = null


    def onOpen(connection: WebSocketConnection) {
        connection.send("Hello! There are " + connectionCount + " other connections active")
        connectionCount += 1
        log.debug("WebSocket connection %s (no: %s) opened".format(connection, connectionCount))

        while (connectionCount > 0) {
            if (isBSD) {
                val raw =  SvdLowLevelSystemAccess.usagesys(SvdConfig.defaultUserUID) //.split("|")
                // val pu = SvdProcessUsage(pid = raw(0).toInt, name = raw(2), rss = raw(4).toInt)
                connection.send("%d<br/>%s".format(connectionCount, raw.replaceAll("\n","<br/>")))
                Thread.sleep(500)
            }
        }
    }

    def onClose(connection: WebSocketConnection) {
        log.debug("WebSocket connection closed: " + connection)
        connectionCount -= 1
    }

    def onMessage(connection: WebSocketConnection, message: String) {
        log.debug("WebSocket text message (" + message + ") received on connection: " + connection)
        connection.send(message.toUpperCase)
    }

    def onMessage(connection: WebSocketConnection, message: Array[Byte]) {
        log.debug("WebSocket byte message (" + message + ") received on connection: " + connection)
    }

    def onPong(connection: WebSocketConnection, message: String) {
        log.debug("WebSocket pong message (" + message + ") received on connection: " + connection)
    }

}
