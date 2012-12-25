/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.notifications


/**
 * Abstract interface for all notification gates
 *
 * @author teamon
 */
trait Gate {
    def connect
    def disconnect
    def setStatus(s: String)
    def send(message: String)
}
