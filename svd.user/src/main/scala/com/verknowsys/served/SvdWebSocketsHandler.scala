/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served


import com.verknowsys.served.utils._
import org.webbitserver._
import scala.collection.mutable


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
    pid: Int,
    name: String,
    rss: Int
    )


class SvdProcessQueue extends DQueue[SvdProcessUsage] {


    def N = 60 * 15 // 15 minutes data from each second to keep in queue


    def K = 60 // standarisation/ normalization after each 60 seconds


    def average(elems: Seq[SvdProcessUsage]) = {
        // elems.reduceLeft(SvdProcessUsage(pid = _.pid, name = _.name, rss = _.rss + _.rss)) / K // XXX: hack - for resident set size now
        SvdProcessUsage(name = "SSS", pid = 666, rss = 0)
    }


}


class SvdWebSocketsHandler extends BaseWebSocketHandler with Logging with SvdUtils {

    import com.verknowsys.served.systemmanager.native._

    // var queue: SvdProcessQueue = null


    override def onOpen(connection: WebSocketConnection) {
        // connection.send("Hello! There are " + connectionCount + " other connections active")
        log.debug("WebSocket connection opened".format(connection))

        // while (true) {
            if (isBSD) {
                val raw =  SvdLowLevelSystemAccess.usagesys(SvdConfig.defaultUserUID) //.split("|")
                // val pu = SvdProcessUsage(pid = raw(0).toInt, name = raw(2), rss = raw(4).toInt)
                connection.send("%d<br/>%s".format(123, raw.replaceAll("\n","<br/>")))
            } else {
                connection.send(new java.util.Date().toString)
            }
            // Thread.sleep(500) // XXX: hardcode
        // }
    }

    override def onClose(connection: WebSocketConnection) {
        log.debug("WebSocket connection closed: " + connection)

    }

    override def onMessage(connection: WebSocketConnection, message: String) {
        log.debug("WebSocket text message (" + message + ") received on connection: " + connection)
        connection.send(message.toUpperCase)
    }

    override def onMessage(connection: WebSocketConnection, message: Array[Byte]) {
        log.debug("WebSocket byte message (" + message + ") received on connection: " + connection)
    }

    // override def onPong(connection: WebSocketConnection, message: String) {
    //     log.debug("WebSocket pong message (" + message + ") received on connection: " + connection)
    // }

}
