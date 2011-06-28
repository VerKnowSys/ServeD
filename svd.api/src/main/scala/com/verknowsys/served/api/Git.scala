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

case class Repository(
    name: String,                       // the name of repository
    authorizedKeys: List[Int] = Nil,    // list of keys with access to this repository
    uuid: UUID = randomUUID
) extends Persistent


// messages

case class GetRepositoryByName(name: String) extends Base
case class GetRepositoryByUUID(uuid: UUID) extends Base
    

case class CreateRepository(name: String) extends Base
    // Repository
    case object RepositoryExistsError


case class UpdateRepositoryConfiguration(repository: Repository) extends Base
    // Success


case class RemoveRepository(uuid: UUID) extends Base
    // Success
    case object RepositoryDoesNotExistError


case object ListRepositories extends Base
    case class Repositories(repositories: List[Repository])

