package com.verknowsys.served.systemmanager

import com.sun.jna.{Native, Library}


/**
 *   POSIX enum definition
 * 
 * @author dmilith
 */
object POSIX extends Enumeration(initial = 1) {
    type POSIX = Value
    @specialized val SIGHUP, // 2010-10-05 15:03:27 - dmilith - NOTE: should have value 1
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
