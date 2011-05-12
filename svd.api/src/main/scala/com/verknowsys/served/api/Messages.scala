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
    case class Connect(username: String) extends Base
    case object Disconnect extends Base
}


object Git {
    sealed abstract class Base extends ApiMessage
    
    // Request
    case class CreateRepository(val name: String) extends Base
    case class RemoveRepository(val name: String) extends Base
    case object ListRepositories extends Base
    case class ShowRepository(val name: String) extends Base
    

    // Response
    case object RepositoryExistsError
    case object RepositoryDoesNotExistError
    case class Repositories(val list: List[String])
    case class Repository(val name: String)
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
        cls: String,
        // homeAddress: Option[java.net.InetSocketAddress],
        status: String,
        // linkedActors: List[ActorInfo],
        linkedActors: List[ActorInfo]  
        // mailboxSize: Int
    )
}





