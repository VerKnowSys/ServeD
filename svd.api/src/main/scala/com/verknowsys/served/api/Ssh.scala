package com.verknowsys.served.api


import java.security.PublicKey

/**
 *  @author teamon
 *
 *   SSH related API
 */

case class AccessKey(name: String, key: PublicKey){
    override def equals(that: Any) = that match {
        case AccessKey(n, k) => k == k
        case _ => false
    }

    override def hashCode = key.hashCode
}
