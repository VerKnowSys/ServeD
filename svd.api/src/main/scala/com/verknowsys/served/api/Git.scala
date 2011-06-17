package com.verknowsys.served.api

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
