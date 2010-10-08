package com.verknowsys.served.api


// ServeD -> Client messages
// common responses
case object Success
case class Error(val message: String)


object Git {
    // Client -> ServeD messages
    case class CreateRepository(val name: String)
    case class RemoveRepository(val name: String)
    case object ListRepositories
    case class ShowRepository(val name: String)
    

    // ServeD response
    case object RepositoryExistsError
    case class Repositories(val list: List[Repository])
    case class Repository(val name: String)
}
