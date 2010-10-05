// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.systemmanager


import com.verknowsys.served.utils._

import com.sun.jna.Library
import com.sun.jna.Native


object SvdSystemManager extends Actor with Utils {

    def act {
        Actor.loop {
            receive {
                case SendSignal(x) =>
                    logger.trace("Send Signal Request received with signal %s".format(x))
                
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


    def sendSignalToPid(signal: POSIXSignals.Value, pid: Int) =
        signal match {
            case POSIXSignals.SIGHUP =>
                logger.trace("SigHUP sent to process pid: %s")
                
            case POSIXSignals.SIGINT =>
                logger.trace("SigINT sent to process pid: %s")
                
            case POSIXSignals.SIGQUIT =>
                logger.trace("SigQUIT sent to process pid: %s")
                
            case POSIXSignals.SIGKILL =>
                logger.trace("SigKILL sent to process pid: %s")
                
        }

}
