package com.verknowsys.served.api


import com.verknowsys.served.api.pools._
import SvdPoolRandomized._


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
        gid: Int,
        information: String = "",
        shell: String = "/bin/sh",
        uuid: UUID = randomUUID
    ) extends Persistent {
    
    override def toString = "SvdAccount(" + userName + ")"

}


case class GetAccount(uid: Int)


/**
 *  @author dmilith
 *
 *   SvdUserPort describes one of port from user pool (defined in SvdPools)
 */
case class SvdUserPort(
        number: Int = randomUserPort,
        uuid: UUID = randomUUID
    ) extends Persistent


/**
 *  @author dmilith
 *
 *   SvdSystemPort describes one of port from system pool (defined in SvdPools)
 */
case class SvdSystemPort(
        number: Int = randomSystemPort,
        uuid: UUID = randomUUID
    ) extends Persistent


/**
 *  @author dmilith
 *
 *   SvdUID stores system uid and human readable name related to it
 */
case class SvdUID(
        number: Int = randomUserUid,
        name: String = "",
        uuid: UUID = randomUUID
    ) extends Persistent


/**
 *  @author dmilith
 *
 *   SvdGID stores system gid and human readable name related to it
 */
case class SvdGID(
        number: Int = randomUserGid,
        name: String = "",
        uuid: UUID = randomUUID
    ) extends Persistent


// 2011-06-25 23:13:38 - dmilith - PENDING: user domain issues
// /**
//  *  @author dmilith
//  *
//  *   SvdUserDomain describes user domain registered by user
//  */
// case class SvdUserDomain(name: String, uuid: UUID = randomUUID) extends Persistent
