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
class Worker(ci: ActorRef, tasks: List[Task]) extends Actor {
    log.debug("Starting Worker with tasks: %s", tasks)
    
    def receive = waiting(Nil, tasks)

    def waiting(history: List[ProcessFinished], tasks: List[Task]): Receive = {
        case Build =>
            log.trace("Worker received Build")
            tasks match {
                case task :: rest =>
                    become(waiting(history, rest))
                    runTask(task)
                case Nil =>
                    ci ! BuildSucceed(history)
                    self stop // TODO
                    
            }

        case res @ ProcessFinished(exitCode, stdout, stderr) =>
            log.trace("Worker received: %s", res)
            if(exitCode == 0) {
                become(waiting(res :: history, tasks))
                self ! Build
            } else {
                ci ! BuildFailed(res :: history)
                self stop // TODO
            }
    }
    
    protected def runTask(task: Task) {
        log.debug("Running task: %s", task)
        
        // TODO: Spawn new Process with task.cmd command as parameter
    }
}
