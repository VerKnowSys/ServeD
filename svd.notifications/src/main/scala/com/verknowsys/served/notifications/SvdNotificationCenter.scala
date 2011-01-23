package com.verknowsys.served.notifications
    
import com.verknowsys.served._
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.git._
import com.verknowsys.served.utils.signals._

import akka.actor.Actor


object SvdNotificationCenter extends Actor with Logging {
    case class Message(message: String)
    case class Status(status: String)
    
    val gates: List[Gate] = new SvdXMPPGate(
        SvdConfig.notificationXmppHost, 
        SvdConfig.notificationXmppPort,
        SvdConfig.notificationXmppLogin,
        SvdConfig.notificationXmppPassword,
        SvdConfig.notificationXmppResource
    ) :: Nil
    
    override def preStart {
        gates.foreach(_.connect)
    }

    def receive = {
        case Status(status) =>
            log.info("Setting status %s", status)
            gates.foreach(_ setStatus status)
            
        case Message(msg) =>
            log.info("Sending message %s", msg)
            gates.foreach(_ send msg)
        
        case msg => 
            log.warn("Message not recoginzed: %s", msg)
    }
    
    override def postStop {
        gates.foreach(_.disconnect)
    }
}  

