package com.verknowsys.served.notifications

import scala.actors._
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
    
import com.verknowsys.served._
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.git._
import com.verknowsys.served.utils.signals._

object NotificationCenter extends CommonActor {
    case class Message(message: String)
    case class Status(status: String)
    
    start
    
    // XXX: Hardcoded gate
    val gates = new XMPPGate(
        Config("xmpp.host") or "localhost", 
        Config("xmpp.port") or 5222,
        Config("xmpp.login") or "gitbot",
        Config("xmpp.password") or "git-bot-666",
        Config("xmpp.resource") or "served-bot-resource"
    ) :: Nil
    

    def act {
        loop {
            receive {
                case Init =>                        
                    info("NotificationCenter connecting gates")
                    gates.foreach { _.connect }                                    
                    info("NotificationCenter ready")
                    
                case Quit =>
                    info("Quitting NotificationCenter")
                    gates.foreach { _.disconnect }
                    exit
            
                case Status(status) => 
                    info("NotificationCenter ! Status(%s)".format(status))
                    gates.foreach { _ setStatus status }
                
                case Message(msg) => 
                    info("NotificationCenter ! Message(%s)".format(msg))
                    gates.foreach { _ send msg }
                
                case _ => messageNotRecognized(_)
            }
        }
    }
    
    override def toString = "NotificationCenter"
}  

trait Gate {
    def connect
    def disconnect
    def setStatus(s: String)
    def send(message: String)
}