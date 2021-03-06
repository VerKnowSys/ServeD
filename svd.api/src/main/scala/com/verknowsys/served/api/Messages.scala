/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.api


import akka.actor.ActorRef


/*sealed*/ abstract class ApiMessage
/*sealed*/ abstract class ApiResponse extends ApiMessage

// ServeD -> Client messages
// common responses

case object Shutdown extends ApiResponse
case class ApiSuccess(message: String = "API request completed successfully", content: Option[String] = None) extends ApiResponse
case class ApiError(message: String = "API request failed", content: Option[String] = None) extends ApiResponse

// General errors
case object NotImplementedError

object Notify {
    // generic notification center messages:
    sealed abstract class Base extends ApiMessage

    case object Connect extends Base
    case object Disconnect extends Base

    case object Ping extends Base
    case object Pong extends Base

    case class Message(content: String) extends Base
    case class Status(content: String) extends Base
}


object General {
    sealed abstract class Base extends ApiMessage

    // Request
    case object CreateSession extends Base
    case class Connect(userUid: Int) extends Base
    case object Disconnect extends Base

    case object GetStatus extends Base

    object Status {
        case object Connected
        case object Disconnected
    }
}


object Admin {
    sealed abstract class Base extends ApiMessage

    case object RespawnAccounts extends Base
    case class RegisterAccount(name: String) extends Base
    case class GetAccountManager(userUid: Int) extends Base

    case class GetSysUsage(userUid: Int) extends Base
    case class Alive(account: SvdAccount) extends Base
    case class Dead(account: SvdAccount) extends Base
    case object AliveAccounts extends Base

    // Request
    case object ListTreeActors extends Base
    case object ListActors extends Base

    // Response
    case class ActorsList(list: Array[ActorInfo])

    case class ActorInfo(
        uuid: String,
        className: String,
        status: String,
        linkedActors: List[ActorInfo]
    )
}


object System {
    sealed abstract class Base extends ApiMessage

    case object GetRunningProcesses extends Base
    case object GetNetstat extends Base

    case class GetUserProcesses(uid: Int) extends Base
    case class SpawnProcess(cmd: String) extends Base
    case class KillProcess(what: Int, signal: Any) extends Base
    case class Chmod(what: String, mode: Int, recursive: Boolean) extends Base
    case class Chown(what: String, userId: Int, recursive: Boolean) extends Base

    case class RegisterDomain(domain: String, proxyActor: ActorRef) extends Base
    case object RegisterUserPort extends Base
    case object GetPort extends Base
}
