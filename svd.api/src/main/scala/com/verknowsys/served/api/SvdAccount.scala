package com.verknowsys.served.api

import com.verknowsys.served.api._
import com.verknowsys.served.api.pools._


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

    case object GetServicePort
    case class GetServicePort(name: String) extends Base
    case class GetServiceStatus(name: String) extends Base
    case class ReadLogFile(serviceName: String, pattern: Option[String]) extends Base
    case class SpawnService(name: String) extends Base
    case class TerminateService(name: String) extends Base


    /**
     * @author Daniel (dmilith) Dettlaff
     *
     *  This message is used in communication between user core and WebAPI. It allows to set a file event on a file and perform actions defined in SvdService on a trigger. Flags constants are defined in SvdFileEventsManager.
     *
     * @warning fileToWatch parammeter will be prepended by default user's home directory, because it's the only place where user can create his file watches.
     */
    case class CreateFileWatch(fileToWatch: String, flags: Int, serviceName: String) extends Base


    /**
     * @author Daniel (dmilith) Dettlaff
     *
     *  This message will destroy all file watches on given file (of course if owned by user)
     *
     */
    case class DestroyFileWatch(fileToUnwatch: String) extends Base


    /**
     * @author Daniel (dmilith) Dettlaff
     *
     *  Call to store domain record for user.
     *
     */
    case class StoreUserDomain(domain: String) extends Base

    /**
     * @author Daniel (dmilith) Dettlaff
     *
     *  Call to retrieve stored domain records for user.
     *
     */
    case object RegisteredDomains extends Base


    /**
     * @author Daniel (dmilith) Dettlaff
     *
     *  Call to perform user side, writable copy of already defined Igniter.
     *
     */
    case class CloneIgniterForUser(igniterName: String, userIgniterName: Option[String] = None) extends Base

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

    override def toString = "SvdAccount(%s)[%s]{%s}".format(userName, uid, accountManagerPort)

}


/**
 *  @author dmilith
 *
 *   SvdUserPort describes one of port from user pool (defined in SvdPools)
 */
case class SvdUserPort(
        number: Int,
        uuid: UUID = randomUUID
    ) extends Persistent {
        override def toString = "SvdUSerPort(" + number + ")"
    }


/**
 *  @author dmilith
 *
 *   SvdSystemPort describes one of port from system pool (defined in SvdPools)
 */
case class SvdSystemPort(
        number: Int,
        uuid: UUID = randomUUID
    ) extends Persistent {
        override def toString = "SvdSystemPort(" + number + ")"
    }


/**
 *  @author dmilith
 *
 *   SvdUID stores system uid and human readable name related to it
 */
case class SvdUserUID(
        number: Int,
        name: String = "",
        uuid: UUID = randomUUID
    ) extends Persistent {
        override def toString = "SvdUSerUID(" + number + ")"
    }


// 2011-06-25 23:13:38 - dmilith - PENDING: user domain issues
/**
 *  @author dmilith
 *
 *   SvdUserDomain describes user domain registered by user
 */
case class SvdUserDomain(
        name: String,
        wildcard: Boolean = true,
        uuid: UUID = randomUUID
    ) extends Persistent {
        override def toString = "SvdUserDomain(%s)[wldcrd: %s]".format(name, wildcard)
    }


/**
 * @author Daniel (dmilith) Dettlaff
 *
 *  This class contains path -> service name binding used in FEM triggers.
 *
 */
case class SvdFileEventBinding(
        absoluteFilePath: String,
        serviceName: String,
        flags: Int,
        uuid: UUID = randomUUID
    ) extends Persistent {
        override def toString = "SvdFileEventBinding(%s triggers service: %s)".format(absoluteFilePath, serviceName)
    }

