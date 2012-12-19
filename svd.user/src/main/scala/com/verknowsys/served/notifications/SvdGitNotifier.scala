package com.verknowsys.served.notifications


import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
    
import com.verknowsys.served._
import com.verknowsys.served.utils._
import com.verknowsys.served.git.GitRepository
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.utils.Logging

import java.text.SimpleDateFormat

import akka.actor.Actor


class SvdGitNotifier(repo: GitRepository) extends Actor with Logging with SvdActor {

    var oldHEAD = repo.head // XXX: var :(

    def receive = {
        case _ =>
    }

    // def act {
    //     log.trace("Git head path: " + repo.headPath)
    //     
    //     def notifyAboutNewHead {
    //         log.trace("HEAD changed in repo: %s".format(repo.dir))
    //         
    //         repo.history(oldHEAD).toList.reverse.foreach { commit =>
    //             log.trace("Commit: " + commit)
    //             val message = "%s\n%s %s\n%s".format(commit.sha, new SimpleDateFormat("yyyy-MM-dd HH:mm").format(commit.date), commit.author.nameAndEmail, commit.message)
    //             SvdNotificationCenter ! Status("ServeD Git Bot Notifier (last: %s)".format(commit.sha))
    //             SvdNotificationCenter ! Message(message)
    //         }
    // 
    //         log.trace("OldHead sha: %s".format(oldHEAD))
    //         oldHEAD = repo.head
    //         log.trace("Assigned new sha: %s to oldHead".format(oldHEAD))
    //     }
    // 
    //     // different git bahaviour on Linux and Mac
    //     // TODO: Fix this, need to check git`s bahaviour
    //     // val watchHEAD =  if(isLinux) {
    //     //     FileEvents.watchRenamed(repo.headPath){ (oldFileName, newFileName) => 
    //     //         if(newFileName.contains(repo.headFile)) notifyAboutNewHead 
    //     //     }
    //     // } else if(isMac) {
    //     //     FileEvents.watchModified(repo.headPath){ (fileName) => 
    //     //         if(fileName.contains(repo.headFile)) notifyAboutNewHead 
    //     //     }
    //     // } else {
    //     //     error("OS Not supported!")
    //     // }
    //     
    // 
    //     loop {
    //         receive {
    //             case Init => 
    //                 info("Git Notifier ready")
    //             
    //             case Quit => 
    //                 info("Quitting Git Notifier")
    //                 exit
    // 
    //             case _ => messageNotRecognized(_)
    //         }
    //     }
    // }

}
