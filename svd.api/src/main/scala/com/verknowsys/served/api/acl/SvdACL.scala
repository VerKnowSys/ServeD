/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.api.acl


import com.verknowsys.served.api._


/**
 *  @author dmilith
 *
 *   Describes general ACL (all defined ACLs should inherit from it)
 */
trait SvdACL extends Persistent

    
/**
 *  @author dmilith
 *
 *   Include this ACL to grant permission for execution of processes given as param list
 */
case class ExecutionAllowed(
        of: List[String],
        uuid: UUID = randomUUID
    ) extends SvdACL


/**
*   @author dmilith
*
*   Include this ACL to allow to use root account to spawn/ access to file
*/
case class RootAllowed(
        uuid: UUID = randomUUID
    ) extends SvdACL


/**
*   @author dmilith
*
*   Include this ACL when user is allowed to log in on some services
*/
case class LoginAllowed(
        to: List[String],
        uuid: UUID = randomUUID
    ) extends SvdACL


/**
*   @author dmilith
*
*   Include this ACL to start "one-process-account"
*/
case class OneProcAllowed(
        name: String,
        uuid: UUID = randomUUID
    ) extends SvdACL


/**
*   @author dmilith
*
*   Include this ACL to allow user to login with ssh 2
*/
case class SSHAllowed(
        uuid: UUID = randomUUID
    ) extends SvdACL
