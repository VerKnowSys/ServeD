package com.verknowsys.served.api.git

import com.verknowsys.served.api._
import java.security.PublicKey

/**
 * This module is related to operations on git repositories
 *
 * @author teamon
 */
sealed abstract class Base extends ApiMessage


// data

case class Repo(
    name: String,                           // the name of repository
    authorizedKeys: List[Int] = Nil,  // list of keys with access to this repository
    uuid: UUID = randomUUID
) extends Persistent


// messages

case class CreateRepository(name: String) extends Base
    // Repo
    case object RepositoryExistsError


case class UpdateRepositoryConfiguration(repository: Repo) extends Base
    // Success


case class RemoveRepository(uuid: UUID) extends Base
    // Success
    case object RepositoryDoesNotExistError


case object ListRepositories extends Base
    case class Repositories(repositories: List[Repo])
   
    
        
    
    
    
    
    
    
    
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
