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
    log.debug("Starting Worker with tasks: %s", tasks)
    
    def receive = waiting(Nil, tasks, None)

    def waiting(history: List[ProcessFinished], tasks: List[Task], ci: Option[ActorRef]): Receive = {
        case Build =>
            log.trace("Worker received Build")
            tasks match {
                case task :: rest =>
                    become(waiting(history, rest, ci orElse self.sender))
                    runTask(task)
                case Nil =>
                    (ci orElse self.sender) foreach { _ ! BuildSucceed(history) }
                    self stop // TODO
            }

        case res @ ProcessFinished(exitCode, stdout, stderr) =>
            log.trace("Worker received: %s", res)
            if(exitCode == 0) {
                become(waiting(res :: history, tasks, ci))
                self ! Build
            } else {
                ci.foreach { _ ! BuildFailed(res :: history) }
                self stop // TODO
            }
    }
    
    protected def runTask(task: Task) {
        log.debug("Running task: %s", task)
        
        // TODO: Spawn new Process with task.cmd command as parameter
    }
}
