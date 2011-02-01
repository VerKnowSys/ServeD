package com.verknowsys.served.systemmanager.acl


/**
 *  @author dmilith
 *
 *   This case class contains ACL traits for SvdAccount security
 */
class SvdACL


/**
 *  @author dmilith
 *
 *   Include this ACL to grant permission for execution of process
 */
trait ExecutionAllowed {
    this: SvdACL =>
}


/**
*   @author dmilith
*
*   Include this ACL to allow to use root account to spawn/ access to file
*/
trait RootAllowed {
    this: SvdACL =>
}


/**
*   @author dmilith
*
*   Include this ACL when user is allowed to log in
*/
trait LoginAllowed {
    this: SvdACL =>
}


/**
*   @author dmilith
*
*   Include this ACL to start "one-process-account"
*/
trait OneProcAllowed {
    this: SvdACL =>
}


/**
*   @author dmilith
*
*   Include this ACL to allow user to login with ssh 2
*/
trait SSHAllowed {
    this: SvdACL =>
}

