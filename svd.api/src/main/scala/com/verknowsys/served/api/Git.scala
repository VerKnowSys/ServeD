package com.verknowsys.served.api

import java.security.PublicKey

/**
 * This module is related to operations on git repositories
 *
 * @author teamon
 */
package git {
    sealed abstract class Base extends ApiMessage
    
    
    // @data
    case class Repository(
        name: String,                           // the name of repository
        authorizedKeys: List[PublicKey] = Nil,  // list of keys with access to this repository
        uuid: UUID = randomUUID
    ) extends Persistent
    
    
    // @messages
    case class CreateRepository(name: String) extends Base
        // Success
        case object RepositoryExistsError
    
    
    case class UpdateRepositoryConfiguration(repository: Repository) extends Base
        // Success
    
    
    case class RemoveRepository(uuid: UUID) extends Base
        // Success
        case object RepositoryDoesNotExistsError
    
    
    case object ListRepositories extends Base
        case class Repositories(repositories: List[Repository])
    
    
        
    
    
    
    
    
    
    
    // Request
    // case class CreateRepository(val name: String) extends Base
    // case class RemoveRepository(val name: String) extends Base
    // case object ListRepositories extends Base
    // case class ShowRepository(val name: String) extends Base
    // 
    // 
    // // Response
    // case object RepositoryExistsError
    // case object RepositoryDoesNotExistError
    // case class Repositories(val list: List[String])
    // case class Repository(val name: String)
}
