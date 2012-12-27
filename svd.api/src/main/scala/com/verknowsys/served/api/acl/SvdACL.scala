/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.api.acl


import com.verknowsys.served.api._


/**
 *   Describes general ACL (all defined ACLs should inherit from it)
 *
 *  @author dmilith
 */
trait SvdACL extends Persistent

    
/**
 *   Include this ACL to grant permission for execution of processes given as param list
 *
 *  @author dmilith
 */
case class ExecutionAllowed(
        of: List[String],
        uuid: UUID = randomUUID
    ) extends SvdACL


/**
*   Include this ACL to allow to use root account to spawn/ access to file
*
*   @author dmilith
*/
case class RootAllowed(
        uuid: UUID = randomUUID
    ) extends SvdACL


/**
*   Include this ACL when user is allowed to log in on some services
*
*   @author dmilith
*/
case class LoginAllowed(
        to: List[String],
        uuid: UUID = randomUUID
    ) extends SvdACL


/**
*   Include this ACL to start "one-process-account"
*
*   @author dmilith
*/
case class OneProcAllowed(
        name: String,
        uuid: UUID = randomUUID
    ) extends SvdACL


/**
*   Include this ACL to allow user to login with ssh 2
*
*   @author dmilith
*/
case class SSHAllowed(
        uuid: UUID = randomUUID
    ) extends SvdACL
