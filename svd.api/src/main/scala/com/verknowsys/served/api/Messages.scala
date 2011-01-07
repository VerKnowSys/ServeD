package com.verknowsys.served.api

sealed abstract class ApiMessage
// ServeD -> Client messages
// common responses
case object Success
case class Error(val message: String)

// General errors
case object NotImplementedError


object Git {
    sealed abstract class Base extends ApiMessage
    // Client -> ServeD messages
    case class CreateRepository(val name: String) extends Base
    case class RemoveRepository(val name: String) extends Base
    case object ListRepositories extends Base
    case class ShowRepository(val name: String) extends Base
    

    // ServeD response
    case object RepositoryExistsError
    case class Repositories(val list: List[Repository])
    case class Repository(val name: String)
}
