// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.systemmanager


import com.verknowsys.served.systemmanager._
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.signals._

import scala.actors.Actor
import com.sun.jna.Library
import com.sun.jna.Native


/**
*   @author dmilith
*   
*   SystemManager - responsible for System Managment and Monitoring
*/
object SvdSystemManager extends Actor with Utils {

    def act {
        Actor.loop {
            receive {
                case Init =>
                    logger.info("SystemManager ready")
                    
                case Quit =>
                    logger.info("Quitting SystemManager…")
                
                case SendSignal(signal, pid) =>
                    sendSignalToPid(signal, pid)
                    logger.trace("Send Signal Request, received with signal %s, for pid: %s".format(signal, pid))
                
                case x: AnyRef =>
                    logger.trace("Command not recognized. SystemManager will ignore You: %s".format(x))
                    
            }
        }
    }


    /**
    *   @author dmilith
    *   
    *   This function is a bridge to low level libc functions
    */
    def posix = Native.loadLibrary("c", classOf[POSIX]).asInstanceOf[POSIX]


    /**
    *   @author dmilith  
    *   
    *   This function will send given signal (first param), to given pid (second param)
    */
    def sendSignalToPid(signal: POSIXSignals.Value, pid: Int) =
        signal match {
            case POSIXSignals.SIGHUP =>
                logger.trace("SigHUP sent to process pid: %s".format(pid))
                
            case POSIXSignals.SIGINT =>
                logger.trace("SigINT sent to process pid: %s".format(pid))
                
            case POSIXSignals.SIGQUIT =>
                logger.trace("SigQUIT sent to process pid: %s".format(pid))
                
            case POSIXSignals.SIGKILL =>
                logger.trace("SigKILL sent to process pid: %s".format(pid))
            
            case x: AnyRef =>
                logger.trace("Command not recognized. sendSignalToPid will ignore You: %s".format(x))
                
        }

}
