/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.utils.signals


/**
 *  SvdPOSIX enum definition
 * 
 *  @author dmilith
 */
object SvdPOSIX extends Enumeration(initial = 1) {
    
    type SvdPOSIX = Value
    
    
    val SIGHUP, // 2010-10-05 15:03:27 - dmilith - NOTE: will have value 1 instead of default 0
        SIGINT,
        SIGQUIT,
        SIGILL,
        SIGTRAP,
        SIGABRT,
        SIGBUS,
        SIGFPE,
        SIGKILL,
        SIGUSR1,
        SIGSEGV,
        SIGUSR2,
        SIGPIPE,
        SIGALRM,
        SIGTERM,
        SIGSTKFLT,
        SIGCHLD,
        SIGCONT,
        SIGSTOP,
        SIGTSTP,
        SIGTTIN,
        SIGTTOU,
        SIGURG,
        SIGXCPU,
        SIGXFSZ,
        SIGVTALRM,
        SIGPROF,
        SIGWINCH,
        SIGIO,
        SIGPOLL,
        SIGPWR,
        SIGSYS = Value

}
