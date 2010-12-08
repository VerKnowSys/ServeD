package com.verknowsys.served.notifications

import scala.actors._
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
    
import com.verknowsys.served._
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.git._
import com.verknowsys.served.utils.signals._

object NotificationCenter extends Actor with Utils {
    case class Message(message: String)
    case class Status(status: String)
    
    start
    
    // XXX: Hardcoded gate
    val gates = new XMPPGate(
        props("xmpp.host", "localhost"), 
        props.int("xmpp.port", 5222),
        props("xmpp.login", "gitbot"),
        props("xmpp.password", "git-bot-666"),
        props("xmpp.resource", "served-bot-resource")
    ) :: Nil
    

    def act {
        loop {
            receive {
                case Init =>                        
                    logger.info("NotificationCenter connecting gates")
                    gates.foreach { _.connect }                                    
                    logger.info("NotificationCenter ready")
                    reply(Ready)
                    
                case Quit =>
                    logger.info("Quitting NotificationCenter")
                    gates.foreach { _.disconnect }
                    reply(Ready)
            
                case Status(status) => 
                    logger.info("NotificationCenter ! Status(%s)".format(status))
                    gates.foreach { _ setStatus status }
                
                case Message(msg) => 
                    logger.info("NotificationCenter ! Message(%s)".format(msg))
                    gates.foreach { _ send msg }
                
                case x: AnyRef => logger.warn("Command not recognized. NotificationCenter will ignore signal: " + x.toString)
            }
        }
    }
}  

trait Gate {
    def connect
    def disconnect
    def setStatus(s: String)
    def send(message: String)
}