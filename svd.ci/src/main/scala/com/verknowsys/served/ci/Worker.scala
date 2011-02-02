package com.verknowsys.served.ci

import akka.actor.Actor
import akka.actor.ActorRef

/** 
 * CI Worker.
 * 
 * {{{
 * val worker = actorOf(new Worker(Task.Clean :: Task.Update :: Task.Test :: Nil)).start
 * worker ! Build
 * }}}
 * 
 * @param tasks well isn't it obvious
 * @author teamon
 */
class Worker(ci: ActorRef, tasks: List[Task]) extends Actor {

    def receive = waiting(Nil, tasks)

    def waiting(history: List[ProcessFinished], tasks: List[Task]): Receive = {
        case Build =>
            tasks match {
                case task :: rest =>
                    runTask(task)
                    become(waiting(history, rest))
                case Nil =>
                    ci ! BuildSucceed(history)
                    // self stop // TODO
                    
            }

        case res @ ProcessFinished(exitCode, stdout, stderr) =>
            if(exitCode == 0) {
                become(waiting(res :: history, tasks))
            } else {
                ci ! BuildFailed(history)
                // self stop // TODO
            }
    }
    
    protected def runTask(task: Task) {
        // TODO: Spawn new Process with task.cmd command as parameter
    }
}


case object Build
case class ProcessFinished(exitCode: Int, stdout: String, stderr: String)

abstract sealed class BuildResult(history: List[ProcessFinished])
case class BuildFailed(history: List[ProcessFinished]) extends BuildResult(history)
case class BuildSucceed(history: List[ProcessFinished]) extends BuildResult(history)
