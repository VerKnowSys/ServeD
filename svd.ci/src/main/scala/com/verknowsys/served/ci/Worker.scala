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
 * @param ci    Owner actor reference
 * @param tasks List of tasks to run
 * @author teamon
 */
class Worker(tasks: List[Task]) extends Actor {
    log.trace("Starting Worker with tasks: %s", tasks)
    
    def receive = waiting(Nil, tasks, None)

    def waiting(history: List[ProcessFinished], tasks: List[Task], ci: Option[ActorRef]): Receive = {
        case Build =>
            tasks match {
                case task :: rest =>
                    become(waiting(history, rest, ci orElse self.sender))
                    runTask(task)
                case Nil =>
                    (ci orElse self.sender) foreach { _ ! BuildSucceed(history) }
                    self stop
            }

        case res @ ProcessFinished(exitCode, stdout, stderr) =>
            if(exitCode == 0) {
                become(waiting(res :: history, tasks, ci))
                self ! Build
            } else {
                ci.foreach { _ ! BuildFailed(res :: history) }
                self stop
            }
    }
    
    protected def runTask(task: Task){
        log.warn("Not Yet Implemented. Worker#runTask. Called with param: %s", task)
        // TODO: NYI Spawn new Process with task.cmd command as parameter
    }
}
