package com.verknowsys.served.notifications

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
    
import com.verknowsys.served._
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.git._
import com.verknowsys.served.utils.signals._

import akka.actor.Actor
import akka.util.Logging

object SvdNotificationCenter extends Actor with Logging {
    case class Message(message: String)
    case class Status(status: String)
    
    
    
    // XXX: Hardcoded gate
    val gates = new SvdXMPPGate(
        SvdConfig("xmpp.host") or "localhost", 
        SvdConfig("xmpp.port") or 5222,
        SvdConfig("xmpp.login") or "gitbot",
        SvdConfig("xmpp.password") or "git-bot-666",
        SvdConfig("xmpp.resource") or "served-bot-resource"
    ) :: Nil
    

    // def act {
    //     loop {
    //         receive {
    //             case Init =>                        
    //                 info("SvdNotificationCenter connecting gates")
    //                 gates.foreach { _.connect }                                    
    //                 info("SvdNotificationCenter ready")
    //                 
    //             case Quit =>
    //                 info("Quitting SvdNotificationCenter")
    //                 gates.foreach { _.disconnect }
    //                 exit
    //         
    //             case Status(status) => 
    //                 info("SvdNotificationCenter ! Status(%s)".format(status))
    //                 gates.foreach { _ setStatus status }
    //             
    //             case Message(msg) => 
    //                 info("SvdNotificationCenter ! Message(%s)".format(msg))
    //                 gates.foreach { _ send msg }
    //             
    //             case _ => messageNotRecognized(_)
    //         }
    //     }
    // }
    
    def receive = {
        case _ => 
    }
    
    override def toString = "SvdNotificationCenter"
}  

trait Gate {
    def connect
    def disconnect
    def setStatus(s: String)
    def send(message: String)
}