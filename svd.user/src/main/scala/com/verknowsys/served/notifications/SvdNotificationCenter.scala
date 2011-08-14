package com.verknowsys.served.notifications
    
import com.verknowsys.served._
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.utils.Logging

import akka.actor.Actor


class SvdNotificationCenter extends Actor with SvdExceptionHandler {
    log.info("SvdNotificationCenter is loading")    
    
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
    }
    
    override def postStop {
        gates.foreach(_.disconnect)
    }
}  

