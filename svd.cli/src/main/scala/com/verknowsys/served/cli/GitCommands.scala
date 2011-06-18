package com.verknowsys.served.cli

import com.verknowsys.served.api.{Success}
import com.verknowsys.served.api.git._

object GitCommands extends Commands {
    def commands(implicit svd: Svd): PartialFunction[Args, Unit] = {
        case "git" :: xs => xs match {
            case "list" :: Nil => 
                request(ListRepositories) {
                    case Repositories(list) => print(list.map(_.toString))
                }
            //     
            // case "create" :: name :: Nil =>
            //     request(Git.CreateRepository(name)) {
            //         case Success =>
            //             info("Repository %s created", name)
            //         case Git.RepositoryExistsError => 
            //             error("Repository with name %s already exists", name)
            //     }
            //     
            // case ("remove" | "rm") :: name :: Nil =>
            //     if(confirm("Are you sure you want to remove repository %s? This operation cannot be undone!".format(name))){
            //         request(Git.RemoveRepository(name)) {
            //             case Success =>
            //                 info("Repository %s removed", name)
            //             case Git.RepositoryDoesNotExistError =>
            //                 error("Repository with name %s does not exist", name)
            //         }
            //     }
            case _ => error("Command not found. TODO: Display help for git commands")
        }
    }
}
