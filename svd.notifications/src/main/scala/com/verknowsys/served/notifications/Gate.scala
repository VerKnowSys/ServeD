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
