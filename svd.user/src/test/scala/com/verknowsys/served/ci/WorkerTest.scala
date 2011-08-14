package com.verknowsys.served.ci

import com.verknowsys.served.testing._
import Actor._

case class TestTask(c: String) extends Task(c)

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

class WorkerTest extends DefaultTest { 
    it should "return Success with empty history when given empty task list" in {
        val worker = actorOf(new TestWorker(Nil)).start
        worker ! Build
        expectMsg(BuildSucceed(Nil))
        worker should be('shutdown)
    }
    
    it should "return Success with one item in history when given one task" in {
         val worker = actorOf(new TestWorker(TestTask("foo") :: Nil)).start
         worker ! Build
         expectMsg(BuildSucceed(
             ProcessFinished(0, "stdout: foo", "stderr: foo") :: Nil
         ))
         worker should be('shutdown)
     }
     
     it should "return Success with full history reversed when given list" in {
         val tasks = TestTask("a") :: TestTask("b") :: TestTask("c") :: Nil
         val worker = actorOf(new TestWorker(tasks)).start
         worker ! Build
         expectMsg(BuildSucceed(
             ProcessFinished(0, "stdout: c", "stderr: c") ::
             ProcessFinished(0, "stdout: b", "stderr: b") ::
             ProcessFinished(0, "stdout: a", "stderr: a") ::
             Nil
         ))
         worker should be('shutdown)
     }
     
     it should "return Failure when given one failing task" in {
         val worker = actorOf(new TestWorker(TestTask("foo-fail") :: Nil)).start
         worker ! Build
         expectMsg(BuildFailed(
             ProcessFinished(1, "stdout: foo-fail", "stderr: foo-fail") :: Nil
         ))
         worker should be('shutdown)
     }
     
     it should "return Failure with full history when given list of tasks with last one failing" in {
         val tasks = TestTask("good") :: TestTask("nice") :: TestTask("cute") :: TestTask("fail") :: Nil
         val worker = actorOf(new TestWorker(tasks)).start
         worker ! Build
         expectMsg(BuildFailed(
             ProcessFinished(1, "stdout: fail", "stderr: fail") ::
             ProcessFinished(0, "stdout: cute", "stderr: cute") ::
             ProcessFinished(0, "stdout: nice", "stderr: nice") ::
             ProcessFinished(0, "stdout: good", "stderr: good") ::
             Nil
         ))
         worker should be('shutdown)
     }
     
     it should "return Failure with partial history when given list of tasks with middle one failing" in {
         val tasks = TestTask("good") :: TestTask("failing one") :: TestTask("cute") :: TestTask("fail") :: Nil
         val worker = actorOf(new TestWorker(tasks)).start
         worker ! Build
         expectMsg(BuildFailed(
             ProcessFinished(1, "stdout: failing one", "stderr: failing one") ::
             ProcessFinished(0, "stdout: good", "stderr: good") ::
             Nil
         ))
         worker should be('shutdown)
     }
}
