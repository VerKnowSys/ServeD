package com.verknowsys.served.ci

import com.verknowsys.served.spechelpers._
import akka.actor.Actor
import akka.actor.Actor._
import akka.actor.ActorRef
import org.specs._

class TestWorker(ci: ActorRef, tasks: List[Task]) extends Worker(ci, tasks) {
    protected override def runTask(task: Task) {
        log.debug("Running task: %s", task)
        
        val process = actorOf(new TestProcess(self, task.cmd)).start
        process ! Build
    }
}

class TestProcess(val worker: ActorRef, val cmd: String) extends Actor {
    log.debug("Starting TestProcess with command: %s", cmd)
    
    def receive = {
        case Build =>
            log.trace("TestProcess received Build")
            log.debug("Starting process: %s", cmd)
            Thread.sleep(100)
            log.debug("Finished process: %s", cmd)
            worker ! ProcessFinished(0, "stdout: " + cmd, "stderr: " + cmd)
            
    }
}

case class TestTask(name: String) extends Task(name)

class WorkerTest extends Specification with SvdExpectActorSpecification {
    "Worker" should {
        doBefore {
            beforeExpectActor
        }
        
        doAfter {
            afterExpectActor
        }
        
        "return Success with empty history when given empty task list" in {
            val worker = actorOf(new TestWorker(expectActor, Nil)).start
            worker ! Build
            expectActor ? BuildSucceed(Nil)
        }
        
        "return Success with one item in history when given one task" in {
            val worker = actorOf(new TestWorker(expectActor, TestTask("foo") :: Nil)).start
            worker ! Build
            expectActor ? BuildSucceed(ProcessFinished(0, "stdout: foo", "stderr: foo") :: Nil)
        }
        
        "return Success with full history reversed when given list" in {
            val tasks = TestTask("a") :: TestTask("b") :: TestTask("c") :: Nil
            val worker = actorOf(new TestWorker(expectActor, tasks)).start
            worker ! Build
            expectActor ? BuildSucceed(
                ProcessFinished(0, "stdout: c", "stderr: c") ::
                ProcessFinished(0, "stdout: b", "stderr: b") ::
                ProcessFinished(0, "stdout: a", "stderr: a") ::
                Nil
            )
        }
        
    }
}