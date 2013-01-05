/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.utils


/** 
 * A bit smarter infinite loop thread 
 * 
 * Use `start` to start thread and `kill` to stop it
 *
 * @param function to run on each iteration
 * @author teamon
 */
class SvdLoopThread(f: => Unit) extends Thread {
    var keep = true
    
    override def run = while(keep) f
    
    def kill { keep = false }
}
