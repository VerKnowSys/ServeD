// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.notifications

import scala.actors._
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
    
import com.verknowsys.served._
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.git._
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.utils.kqueue.Kqueue

import java.text.SimpleDateFormat

class SvdGitNotifier(repo: GitRepository) extends CommonActor {
    import NotificationCenter._

    var oldHEAD = repo.head // XXX: var :(

    def act {
        logger.trace("Git head path: " + repo.headPath)
        
        def notifyAboutNewHead {
            logger.trace("HEAD changed in repo: %s".format(repo.dir))
            
            repo.history(oldHEAD).toList.reverse.foreach { commit =>
                logger.trace("Commit: " + commit)
                val message = "%s\n%s %s\n%s".format(commit.sha, new SimpleDateFormat("yyyy-MM-dd HH:mm").format(commit.date), commit.author.nameAndEmail, commit.message)
                NotificationCenter ! Status("ServeD Git Bot Notifier (last: %s)".format(commit.sha))
                NotificationCenter ! Message(message)
            }
    
            logger.trace("OldHead sha: %s".format(oldHEAD))
            oldHEAD = repo.head
            logger.trace("Assigned new sha: %s to oldHead".format(oldHEAD))
        }

        // different git bahaviour on Linux and Mac
        // TODO: Fix this, need to check git`s bahaviour
        // val watchHEAD =  if(isLinux) {
        //     FileEvents.watchRenamed(repo.headPath){ (oldFileName, newFileName) => 
        //         if(newFileName.contains(repo.headFile)) notifyAboutNewHead 
        //     }
        // } else if(isMac) {
        //     FileEvents.watchModified(repo.headPath){ (fileName) => 
        //         if(fileName.contains(repo.headFile)) notifyAboutNewHead 
        //     }
        // } else {
        //     logger.error("OS Not supported!")
        // }
        

        loop {
            receive {
                case Init => 
                    logger.info("Git Notifier ready")
                
                case Quit => 
                    logger.info("Quitting Git Notifier")
                    exit

                case _ => messageNotRecognized(_)
            }
        }
    }

}
