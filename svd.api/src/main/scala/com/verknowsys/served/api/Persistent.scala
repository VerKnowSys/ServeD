/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.api

/**
 * Include this trait if you want to persist object
 *
 * @author teamon
 */
trait Persistent {
    def uuid: java.util.UUID
    
    var createdAt = new java.util.Date
}
