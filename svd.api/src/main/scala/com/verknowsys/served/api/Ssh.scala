/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

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


/**
 * SSHD response to set channel for given uid
 *
 */
case class InitSSHChannelForUID(userUid: Int) extends ApiResponse

// sshd channel taken
case class Taken(byUid: Int) extends ApiResponse
