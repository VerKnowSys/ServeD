package com.verknowsys.served.ci


import akka.testkit.TestActorRef
import com.typesafe.config.ConfigFactory
import akka.dispatch._
import akka.pattern.ask
import akka.remote._
import akka.util.Duration
import akka.util.Timeout
import akka.testkit.TestKit
import akka.util.duration._
import akka.actor.ActorSystem
import akka.actor.Props


import com.verknowsys.served._
import com.verknowsys.served.utils._
import com.verknowsys.served.api.Logger
import com.verknowsys.served.managers.LoggingManager
import com.verknowsys.served.testing._


case class TestTask(c: String) extends Task(c)

class TestWorker(tasks: List[Task]) extends Worker(tasks) {
    protected override def runTask(task: Task) {
        val process = context.actorOf(Props(new TestProcess(self, task.cmd)))
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


class WorkerTest(_system: ActorSystem) extends TestKit(_system) with DefaultTest {

    def this() = this(ActorSystem("svd-test-system"))
    implicit val timeout = Timeout(30 seconds)


    it should "return Success with empty history when given empty task list" in {
        val worker = system.actorOf(Props(new TestWorker(Nil)))
        (worker ? Build) onSuccess {
            case BuildSucceed(x) =>
                x must be(Nil)
                // worker should be('shutdown)

            case x =>
                fail("Shouldn't happen")
        }

    }


    it should "return Success with one item in history when given one task" in {
        val worker = system.actorOf(Props(new TestWorker(TestTask("foo") :: Nil)))
        (worker ? Build) onSuccess {
            case BuildSucceed(x :: Nil) =>
                x must be(ProcessFinished(0, "stdout: foo", "stderr: foo"))
                // worker should be('shutdown)

            case x =>
                fail("Problem: %s".format(x))
        }
    }


    it should "return Success with full history reversed when given list" in {
        val tasks = TestTask("a") :: TestTask("b") :: TestTask("c") :: Nil
        val worker = system.actorOf(Props(new TestWorker(tasks)))
        (worker ? Build) onSuccess {
            case BuildSucceed(x :: y :: z :: Nil) =>
                x must be(ProcessFinished(0, "stdout: c", "stderr: c"))
                y must be(ProcessFinished(0, "stdout: b", "stderr: b"))
                z must be(ProcessFinished(0, "stdout: a", "stderr: a"))
                // worker should be('shutdown)

            case x =>
                fail("Problem: %s".format(x))
        }
    }


    it should "return Failure when given one failing task" in {
        val worker = system.actorOf(Props(new TestWorker(TestTask("foo-fail") :: Nil)))
        (worker ? Build) onSuccess {
            case BuildFailed(x :: Nil) =>
                x must be(ProcessFinished(1, "stdout: foo-fail", "stderr: foo-fail"))
                // worker should be('shutdown)

            case x =>
                fail("Problem: %s".format(x))
        }
    }


    it should "return Failure with full history when given list of tasks with last one failing" in {
        val tasks = TestTask("good") :: TestTask("nice") :: TestTask("cute") :: TestTask("fail") :: Nil
        val worker = system.actorOf(Props(new TestWorker(tasks)))
        (worker ? Build) onSuccess {
            case BuildFailed(x :: y :: z :: a :: Nil) =>
                x must be(ProcessFinished(1, "stdout: fail", "stderr: fail"))
                y must be(ProcessFinished(0, "stdout: cute", "stderr: cute"))
                z must be(ProcessFinished(0, "stdout: nice", "stderr: nice"))
                a must be(ProcessFinished(0, "stdout: good", "stderr: good"))
                // worker should be('shutdown)

            case x =>
                fail("Problem: %s".format(x))
        }
    }


    it should "return Failure with partial history when given list of tasks with middle one failing" in {
        val tasks = TestTask("good") :: TestTask("failing one") :: TestTask("cute") :: TestTask("fail") :: Nil
        val worker = system.actorOf(Props(new TestWorker(tasks)))
        (worker ? Build) onSuccess {
            case BuildFailed(x :: y :: Nil) =>
                x must be(ProcessFinished(1, "stdout: failing one", "stderr: failing one"))
                y must be(ProcessFinished(0, "stdout: good", "stderr: good"))
                // worker should be('shutdown)

            case x =>
                fail("Problem: %s".format(x))
        }
    }


}
