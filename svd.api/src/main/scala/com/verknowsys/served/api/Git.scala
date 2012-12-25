/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.api.git


import com.verknowsys.served.api._


/**
 * This module is related to operations on git repositories
 *
 * @author teamon
 * @author dmilith
 */

case class Repository(
    name: String,                       // the name of repository
    authorizedKeys: Set[AccessKey] = Set(),    // list of keys with access to this repository
    uuid: UUID = randomUUID
) extends Persistent


object Git {

    sealed abstract class Base extends ApiMessage

    // messages

    case class GetRepositoryByName(name: String) extends Base
    case class GetRepositoryByUUID(uuid: UUID) extends Base


    case class CreateRepository(name: String) extends Base
        // Repository
    case object RepositoryExistsError extends Base


    case class UpdateRepositoryConfiguration(repository: Repository) extends Base
        // Success


    case class RemoveRepository(uuid: UUID) extends Base
        // Success
    case object RepositoryDoesNotExistError extends Base


    case object ListRepositories extends Base
    case class Repositories(repositories: List[Repository]) extends Base

    case class AddAuthorizedKey(uuid: UUID, key: AccessKey) extends Base
    case class RemoveAuthorizedKey(uuid: UUID, key: AccessKey) extends Base

}
