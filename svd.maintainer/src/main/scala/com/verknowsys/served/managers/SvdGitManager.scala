package com.verknowsys.served.managers

// import com.verknowsys.served.utils.SvdUtils
// import com.verknowsys.served.utils.signals.{Init, Quit, Ready}
// import com.verknowsys.served.maintainer.SvdAccount
// import com.verknowsys.served.utils.git.GitRepository
// import com.verknowsys.served.api._
// import com.verknowsys.served.api.Git._

import java.io.File


/**
 * Git SvdManager
 * 
 * @author teamon
 */
class SvdGitSvdManager(owner: SvdAccountSvdManager) extends SvdManager(owner) {
    // def act {
    //     loop {
    //         receive {
    //             // Creates new bare git repository under HOME/git/REPO_NAME
    //             case CreateRepository(name) => 
    //                 trace("Creating new git repository %s for account %s".format(name, account.userName))
    //                 GitRepository.create(gitDir + name, bare = true)
    //                 sender ! Success
    //                 
    //             case RemoveRepository(name) =>
    //                 warn("Unimplemented yet!") // TODO
    //                 sender ! NotImplementedError
    //             
    //             case ListRepositories =>
    //                 sender ! Repositories(GitRepository.list(gitDir).map { r => Repository(r.name) })
    //             
    //             case ShowRepository(name) =>
    //                 warn("Unimplemented yet!") // TODO
    //                 sender ! NotImplementedError
    //             
    //             case Init =>
    //                 info("SvdGitSvdManager ready")
    //                 reply(Ready)
    //                 
    //             case Quit =>
    //                 info("Quitting SvdGitSvdManager")
    //                 reply(Ready)
    //                 exit
    //             
    //             case _ => messageNotRecognized(_)
    //         }
    //     }
    // }

    def receive = {
        case _ =>
    }
    
    protected lazy val gitDir = account.homeDir + "git/"
}
