package com.verknowsys.served.api

import com.verknowsys.served.api._
import com.verknowsys.served.api.pools._


/**
 *  API object for user messages:
 */
object User {

    case object AccountNotFound

    case object SpawnServices
    case object TerminateServices
    case object GetServices

    case class GetAccount(uid: Int)
    case class GetAccountByName(name: String)

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
