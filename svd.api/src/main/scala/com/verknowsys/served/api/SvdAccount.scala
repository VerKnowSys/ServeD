/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.api



object Maintenance {
    abstract class Base extends ApiResponse

    /**
     *  Restart user Account Manager on demand
     *
     * @author Daniel (dmilith) Dettlaff
     */
    case object RestartAccountManager extends Base

}


/**
 *  API object for user messages:
 */
object User {
    abstract class Base extends ApiResponse

    // case object AccountNotFound
    case object SpawnServices extends Base
    case object TerminateServices extends Base
    case object GetServices extends Base // returns List
    case object GetRunningServices extends Base // returns nothing, just notifies
    case object StoreServices extends Base
    case object ShowAvailableServices extends Base

    case object ServiceStatus extends Base // used to get information about Service state
    case object ServiceAutostart extends Base
    case object GetStoredServices extends Base
    case object RemoveAllUserPorts extends Base
    case object GetUserPorts extends Base
    case object GetServicePort extends Base
    case object RegisterUserPort extends Base

    case class GetServicePort(name: String) extends Base
    case class GetServiceStatus(name: String) extends Base
    case class ReadLogFile(serviceName: String, pattern: Option[String]) extends Base
    case class SpawnService(name: String) extends Base
    case class TerminateService(name: String) extends Base


    /**
     *  This message is used in communication between user core and WebAPI. It allows to set a file event on a file and perform actions defined in SvdService on a trigger. Flags constants are defined in SvdFileEventsManager.
     *  fileToWatch parammeter will be prepended by default user's home directory, because it's the only place where user can create his file watches.
     *
     * @author Daniel (dmilith) Dettlaff
     */
    case class CreateFileWatch(fileToWatch: String, flags: Int, serviceName: String) extends Base


    /**
     *  This message will destroy all file watches on given file (of course if owned by user)
     *
     * @author Daniel (dmilith) Dettlaff
     */
    case class DestroyFileWatch(fileToUnwatch: String) extends Base


    /**
     *  Call to store domain record for user.
     *
     * @author Daniel (dmilith) Dettlaff
     */
    case class StoreUserDomain(domain: String) extends Base

    /**
     *  Call to retrieve stored domain records for user.
     *
     * @author Daniel (dmilith) Dettlaff
     */
    case object GetRegisteredDomains extends Base


    /**
     *  Call to perform user side, writable copy of already defined Igniter.
     *
     * @author Daniel (dmilith) Dettlaff
     */
    case class CloneIgniterForUser(igniterName: String, userIgniterName: Option[String] = None) extends Base


    /**
     *  Call to create Mosh server for user.
     *
     * @author Daniel (dmilith) Dettlaff
     */
    case object MoshSession extends Base


    case class GetServiceStdOut(matcher: String = ".*") extends Base // by default match anything
    // case class GetServiceStdErr(matcher: String) extends Base

}


/**
 * SvdAccount describes one virtual svd User
 *
 * @author dmilith
 * @author teamon
 *
 */
case class SvdAccount (
        userName: String = "guest",
        pass: String = "",
        uid: Int,
        accountManagerPort: Int = -1,
        information: String = "",
        shell: String = "",
        uuid: UUID = randomUUID
    ) extends Persistent {

    override def toString = "SvdAccount(%s)[%d]{%d}<%s>".format(userName, uid,
        accountManagerPort, uuid)

}


/**
 *   SvdUserPort describes one of port from user pool (defined in SvdPools)
 *
 *  @author dmilith
 */
case class SvdUserPort(
        number: Int,
        uuid: UUID = randomUUID
    ) extends Persistent {
        override def toString = "SvdUserPort(" + number + ")"
    }


/**
 *   SvdSystemPort describes one of port from system pool (defined in SvdPools)
 *
 *  @author dmilith
 */
case class SvdSystemPort(
        number: Int,
        uuid: UUID = randomUUID
    ) extends Persistent {
        override def toString = "SvdSystemPort(" + number + ")"
    }


/**
 *   SvdUID stores system uid and human readable name related to it
 *
 *  @author dmilith
 */
case class SvdUserUID(
        number: Int,
        name: String = "",
        uuid: UUID = randomUUID
    ) extends Persistent {
        override def toString = "SvdUSerUID(" + number + ")"
    }


/**
 *   SvdUserDomain describes user domain registered by user
 *
 *  @author dmilith
 */
case class SvdUserDomain(
        name: String,
        wildcard: Boolean = true,
        uuid: UUID = randomUUID
    ) extends Persistent {
        override def toString = "SvdUserDomain(%s)[wldcrd: %s]".format(name, wildcard)
    }


/**
 *  This class contains path -> service name binding used in FEM triggers.
 *
 * @author Daniel (dmilith) Dettlaff
 */
case class SvdFileEventBinding(
        absoluteFilePath: String,
        serviceName: String,
        flags: Int,
        uuid: UUID = randomUUID
    ) extends Persistent {
        override def toString = "SvdFileEventBinding(%s triggers service: %s)".format(absoluteFilePath, serviceName)
    }


/**
 *  Security messages collection, related to SvdAccount.
 *
 * @author Daniel (dmilith) Dettlaff
 */
object Security {

    sealed abstract class Base extends ApiMessage

    /**
     *  Basic message to authorize as registered account on remote SvdRoot
     *
     * @author Daniel (dmilith) Dettlaff
     */
    case class GetAccountPriviledges(account: SvdAccount) extends Base

}

