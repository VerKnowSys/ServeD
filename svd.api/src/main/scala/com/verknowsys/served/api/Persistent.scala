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
