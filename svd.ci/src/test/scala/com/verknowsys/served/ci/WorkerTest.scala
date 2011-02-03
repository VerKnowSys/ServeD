package com.verknowsys.served.ci

import com.verknowsys.served.spechelpers._
import akka.actor.Actor
import akka.actor.Actor._
import akka.actor.ActorRef
import org.specs._

class TestWorker(tasks: List[Task]) extends Worker(tasks) {
    protected override def runTask(task: Task) {
        val process = actorOf(new TestProcess(self, task.cmd)).start
        process ! Build
    }
}

class TestProcess(val worker: ActorRef, val cmd: String) extends Actor {
    def receive = {
        case Build =>
            val exitCode = if(cmd.matches(".*fail.*")) 1 else 0
            worker ! ProcessFinished(exitCode, "stdout: " + cmd, "stderr: " + cmd)
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
            val worker = actorOf(new TestWorker(Nil)).start
            worker ! Build
            expectActor ? BuildSucceed(Nil)
            worker must be shutdown
        }
        
        "return Success with one item in history when given one task" in {
            val worker = actorOf(new TestWorker(TestTask("foo") :: Nil)).start
            worker ! Build
            expectActor ? BuildSucceed(
                ProcessFinished(0, "stdout: foo", "stderr: foo") :: Nil
            )
            worker must be shutdown
        }
        
        "return Success with full history reversed when given list" in {
            val tasks = TestTask("a") :: TestTask("b") :: TestTask("c") :: Nil
            val worker = actorOf(new TestWorker(tasks)).start
            worker ! Build
            expectActor ? BuildSucceed(
                ProcessFinished(0, "stdout: c", "stderr: c") ::
                ProcessFinished(0, "stdout: b", "stderr: b") ::
                ProcessFinished(0, "stdout: a", "stderr: a") ::
                Nil
            )
            worker must be shutdown
        }
        
        "return Failure when given one failing task" in {
            val worker = actorOf(new TestWorker(TestTask("foo-fail") :: Nil)).start
            worker ! Build
            expectActor ? BuildFailed(
                ProcessFinished(1, "stdout: foo-fail", "stderr: foo-fail") :: Nil
            )
            worker must be shutdown
        }
        
        "return Failure with full history when given list of tasks with last one failing" in {
            val tasks = TestTask("good") :: TestTask("nice") :: TestTask("cute") :: TestTask("fail") :: Nil
            val worker = actorOf(new TestWorker(tasks)).start
            worker ! Build
            expectActor ? BuildFailed(
                ProcessFinished(1, "stdout: fail", "stderr: fail") ::
                ProcessFinished(0, "stdout: cute", "stderr: cute") ::
                ProcessFinished(0, "stdout: nice", "stderr: nice") ::
                ProcessFinished(0, "stdout: good", "stderr: good") ::
                Nil
            )
            worker must be shutdown
        }
        
        "return Failure with partial history when given list of tasks with middle one failing" in {
            val tasks = TestTask("good") :: TestTask("failing one") :: TestTask("cute") :: TestTask("fail") :: Nil
            val worker = actorOf(new TestWorker(tasks)).start
            worker ! Build
            expectActor ? BuildFailed(
                ProcessFinished(1, "stdout: failing one", "stderr: failing one") ::
                ProcessFinished(0, "stdout: good", "stderr: good") ::
                Nil
            )
            worker must be shutdown
        }
        
    }
}