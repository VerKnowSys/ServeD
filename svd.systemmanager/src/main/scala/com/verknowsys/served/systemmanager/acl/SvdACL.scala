package com.verknowsys.served.systemmanager.acl


/**
*   @author dmilith
*
*   Include this ACL to give access to some functionality
*/
trait OperationAllowed


/**
*   @author dmilith
*
*   Include this ACL to allow to use root account to spawn/ access to file
*/
trait RootAllowed


/**
*   @author dmilith
*
*   Include this ACL when user is allowed to log in
*/
trait LoginAllowed


/**
*   @author dmilith
*
*   Include this ACL to start "one-process-account"
*/
trait OneProcAllowed


/**
*   @author dmilith
*
*   Include this ACL to allow user to login with ssh 2
*/
trait SSHAllowed