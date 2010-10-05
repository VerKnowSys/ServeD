// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.systemmanager


import com.verknowsys.served.utils._

import com.sun.jna.Library
import com.sun.jna.Native


object SystemManager {//extends Utils {


    /**
    *   @author dmilith
    *   
    *   This function is a bridge to low level libc functions
    */
    def posix = Native.loadLibrary("c", classOf[POSIX]).asInstanceOf[POSIX]
    

    def sendSignalToPid(signal: POSIXSignals.Value, pid: Int) =
        signal match {
            case POSIXSignals.SIGHUP =>
                // logger.trace("SigHUP sent to process pid: %s")
            case POSIXSignals.SIGSTOP =>
                // logger.trace("SigHUP sent to process pid: %s")
        }

}
