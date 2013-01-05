/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.cli


object GitCommands extends Commands {
    def commands(implicit svd: Svd): PartialFunction[Args, Unit] = {
        case "git" :: xs => xs match {
            case "list" :: Nil =>
                // request(ListRepositories) {
                //     case Repositories(list) => print(list.map(_.toString))
                // }
            //
            // case "create" :: name :: Nil =>
            //     request(Git.CreateRepository(name)) {
            //         case ApiSuccess =>
            //             info("Repository %s created", name)
            //         case Git.RepositoryExistsError =>
            //             error("Repository with name %s already exists", name)
            //     }
            //
            // case ("remove" | "rm") :: name :: Nil =>
            //     if(confirm("Are you sure you want to remove repository %s? This operation cannot be undone!".format(name))){
            //         request(Git.RemoveRepository(name)) {
            //             case ApiSuccess =>
            //                 info("Repository %s removed", name)
            //             case Git.RepositoryDoesNotExistError =>
            //                 error("Repository with name %s does not exist", name)
            //         }
            //     }
            case _ => error("Command not found. TODO: Display help for git commands")
        }
    }
}
