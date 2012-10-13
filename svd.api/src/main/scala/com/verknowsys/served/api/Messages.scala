package com.verknowsys.served.api


import akka.actor.ActorRef


/*sealed*/ abstract class ApiMessage
/*sealed*/ abstract class ApiResponse extends ApiMessage

// ServeD -> Client messages
// common responses
case object RespawnAccounts extends ApiResponse
case class RegisterAccount(name: String) extends ApiResponse
case object Success extends ApiResponse
case object Shutdown extends ApiResponse
case class Error(val message: String) extends ApiResponse

// General errors
case object NotImplementedError

object Notify {
    // generic notification center messages:
    sealed abstract class Base extends ApiMessage

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

    object Status extends Enumeration {
        val Connected, Disconnected = Value
    }
}

object Admin {
    sealed abstract class Base extends ApiMessage

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

// XXX: Temporary place for those messages
case class GetAccountManager(userUid: Int)
case class SetAccountManager(userUid: Int)
case object GetPort
case class GetSysUsage(userUid: Int)
case class Alive(userUid: Int)