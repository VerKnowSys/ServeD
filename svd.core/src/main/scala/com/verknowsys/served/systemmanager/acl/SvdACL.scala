package com.verknowsys.served.systemmanager.acl


import com.verknowsys.served.api._
import com.verknowsys.served.utils._


/**
 *  @author dmilith
 *
 *   Describes general ACL (all defined ACLs should inherit from it)
 */
case class SvdACL(uuid: UUID = randomUUID) extends Persistent

    
/**
 *  @author dmilith
 *
 *   Include this ACL to grant permission for execution of processes given as param list
 */
case class ExecutionAllowed(of: List[String]) extends SvdACL


/**
*   @author dmilith
*
*   Include this ACL to allow to use root account to spawn/ access to file
*/
case object RootAllowed extends SvdACL


/**
*   @author dmilith
*
*   Include this ACL when user is allowed to log in on some services
*/
case class LoginAllowed(to: List[String]) extends SvdACL


/**
*   @author dmilith
*
*   Include this ACL to start "one-process-account"
*/
case class OneProcAllowed(name: String) extends SvdACL


/**
*   @author dmilith
*
*   Include this ACL to allow user to login with ssh 2
*/
case object SSHAllowed extends SvdACL
