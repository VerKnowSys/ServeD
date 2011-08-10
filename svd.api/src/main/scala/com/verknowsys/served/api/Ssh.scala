package com.verknowsys.served.api

import java.security.PublicKey


/**
 *  SSH related API
 *
 *  @author teamon
 */

case class AccessKey(name: String, key: PublicKey){
    /**
     *  When comparing AccessKeys e.g. inside a Set only /bin/bash: key: command not found matters
     *  @author teamon
     */
    override def equals(that: Any) = that match {
        case AccessKey(n, k) => k == k
        case _ => false
    }

    override def hashCode = key.hashCode
}

/**
 * Generic command for public key access control
 *
 * @response Boolean
 */
case class AuthorizeWithKey(key: PublicKey)
