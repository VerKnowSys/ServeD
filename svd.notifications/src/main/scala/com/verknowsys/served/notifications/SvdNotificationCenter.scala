package com.verknowsys.served.notifications
    
import com.verknowsys.served._
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.git._
import com.verknowsys.served.utils.signals._

import akka.actor.Actor


class SvdNotificationCenter extends Actor {
    log.trace("SvdNotificationCenter is loading")
    
    // XXX: Hardcoded gate
    val gates: List[Gate] = new SvdXMPPGate(
        SvdConfig("xmpp.host") or "localhost", 
        SvdConfig("xmpp.port") or 5222,
        SvdConfig("xmpp.login") or "gitbot",
        SvdConfig("xmpp.password") or "git-bot-666",
        SvdConfig("xmpp.resource") or "served-bot-resource"
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

