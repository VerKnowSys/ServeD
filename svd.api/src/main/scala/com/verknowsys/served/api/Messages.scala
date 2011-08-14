package com.verknowsys.served.api

/*sealed*/ abstract class ApiMessage
/*sealed*/ abstract class ApiResponse extends ApiMessage

// ServeD -> Client messages
// common responses
case object Success extends ApiResponse
case class Error(val message: String) extends ApiResponse

// General errors
case object NotImplementedError

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
case object GetPort
