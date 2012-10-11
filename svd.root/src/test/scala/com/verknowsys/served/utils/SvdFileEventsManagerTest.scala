// package com.verknowsys.served.utils


// import akka.testkit.TestActorRef
// import com.typesafe.config.ConfigFactory
// import akka.dispatch._
// import akka.pattern.ask
// import akka.remote._
// import akka.util.Duration
// import akka.util.Timeout
// import akka.testkit.TestKit
// import akka.util.duration._
// import akka.actor.ActorSystem
// import akka.actor.Props

// import java.io._
// import org.apache.commons.io.FileUtils
// import com.verknowsys.served.utils.events._
// import events._
// import com.verknowsys.served._
// import com.verknowsys.served.api._
// import com.verknowsys.served.api.git._
// import com.verknowsys.served.testing._


// class SvdTestFileEventsReactor extends SvdExpectActor with SvdFileEventsReactor {
//     override def preStart {
//         new File("/tmp/served/file_events_test").mkdir
//         registerFileEventFor("/tmp/served/file_events_test/single", Modified)
//     }
// }

// class SvdTestFileEventsReactorForFile(path: String) extends SvdExpectActor with SvdFileEventsReactor {
//     override def preStart {
//         registerFileEventFor(path, Modified)
//     }
// }


// // class SvdExpectActor extends SvdTestFileEventsReactor {
// //     def receive = {
// //         case FileEvent(path, flags) => // handle event ..

// //         case _ =>
// //             sender ! Success
// //     }
// // }


// case object TestGetIdents

// class TestSvdFileEventsManager extends SvdFileEventsManager {
//     override def receive = super.receive orElse {
//         case TestGetIdents => sender ! idents
//     }
// }


// class SvdFileEventsManagerTest(_system: ActorSystem) extends TestKit(_system) with DefaultTest {

//     def this() = this(ActorSystem("svd-test-system"))

//     final val DIR = "/tmp/served/file_events_test"
//     new File(DIR).mkdir

//     var fem: ActorRef = null


//     override def beforeAll {
//         // beforeExpectActor
//         try { FileUtils.forceDelete(DIR) } catch { case _ => }
//         fem = system.actorOf(Props(new TestSvdFileEventsManager))
//     }

//     override def afterAll {
//         // afterExpectActor
//         system.shutdown
//     }


//     // "SvdFileEventsManager as actor" should {

//         // "start FileEventsManager" in {
//             // registry.actorsFor[TestSvdFileEventsManager] must haveSize(1)
//         // }


//     it should "register new file event using explicit message" in {
//         touch(DIR + "/single")

//         val a = system.actorOf(Props(new SvdExpectActor {
//             fem ! SvdRegisterFileEvent(DIR + "/single", 0x02, self)
//         }))


//         (fem ? TestGetIdents) onSuccess {
//             case Some(idents: SvdFileEventsManager#IdentsMap) => idents.mapValues { case (a,b) => (a, b.toList) } must haveValue( (DIR + "/single", (0x02, expectActor) :: Nil))
//             case _ => fail("timeout")
//         }
//     }


//     it should "register new file event using SvdFileEventsReactor trait" in {
//         touch(DIR + "/single")

//         val expectActor = system.actorOf(Props(new SvdTestFileEventsReactor))
//         senderOption = Some(expectActor)

//         // expectActor ? Success

//         (fem ? TestGetIdents) onSuccess {
//             case Some(idents: SvdFileEventsManager#IdentsMap) => idents.mapValues { case (a,b) => (a, b.toList) } must haveValue( (DIR + "/single", (Modified, expectActor) :: Nil))
//             case x => fail("something went wrong: %s".format(x))
//         }
//     }


//     it should "notify actors when file modified" in {
//         touch(DIR + "/single")

//         expectActor = system.actorOf[SvdTestFileEventsReactor].start
//         senderOption = Some(expectActor)

//         expectActor ? Success

//         writeFile(DIR + "/single", "new content")

//         expectActor ?* (SvdFileEvent(DIR + "/single", 0x04), SvdFileEvent(DIR + "/single", 0x06))
//     }

//     it should "register few events for the same file" in {
//         touch(DIR + "/single")
//         system.actorOf[SvdTestFileEventsReactor].start
//         system.actorOf[SvdTestFileEventsReactor].start
//         // expectActor = system.actorOf[SvdTestFileEventsReactor].start
//         // senderOption = Some(expectActor)

//         expectActor ? Success

//         (fem ? TestGetIdents) onSuccess {
//             case Some(idents: SvdFileEventsManager#IdentsMap) =>
//                 idents must have size(1)
//                 idents.toList.head._2._2 must have size(3)
//             case x => fail("Something went wrong with: %s".format(x))
//         }
//     }

//     it should "register few events for different files" in {
//         touch(DIR + "/one")
//         touch(DIR + "/two")
//         touch(DIR + "/three")
//         touch(DIR + "/four")

//         system.actorOf(Props(new SvdTestFileEventsReactorForFile(DIR + "/one")))
//         system.actorOf(Props(new SvdTestFileEventsReactorForFile(DIR + "/two")))
//         system.actorOf(Props(new SvdTestFileEventsReactorForFile(DIR + "/three")))
//         expectActor = system.actorOf(Props(new SvdTestFileEventsReactorForFile(DIR + "/four")))
//         senderOption = Some(expectActor)

//         expectActor ? Success

//         (fem ? TestGetIdents) onSuccess {
//             case Some(idents: SvdFileEventsManager#IdentsMap) =>
//                 idents must haveSize(4)
//                 idents.toList(0)._2._2 must haveSize(1)
//                 idents.toList(1)._2._2 must haveSize(1)
//                 idents.toList(2)._2._2 must haveSize(1)
//                 idents.toList(3)._2._2 must haveSize(1)
//             case x => fail("Some fail: %s".format(x))
//         }
//     }

//     it should "unregister events when stopped" in {
//         touch(DIR + "/single")

//         expectActor = system.actorOf(Props(new SvdTestFileEventsReactor))
//         senderOption = Some(expectActor)

//         expectActor ? Success

//         expectActor.stop

//         (fem ? TestGetIdents) onSuccess {
//             case Some(idents: SvdFileEventsManager#IdentsMap) => idents must beEmpty
//             case x => fail("Some fail: %s".format(x))
//         }
//     }

//     // TODO: How to test throwing exceptions in other threads?
//     // "raise SvdFileOpenException exception" in {
//     //     {
//     //         expectActor = system.actorOf(new SvdExpectActor {
//     //             fem ! SvdRegisterFileEvent(DIR + "/not-existing-at-all-123", 0x02, self)
//     //         }).start
//     //     } must throwA[SvdFileOpenException]
//     // }


//     it should "unregister file event when stoped" in {
//         touch(DIR + "/single")

//         expectActor = system.actorOf(Props(new SvdTestFileEventsReactor))
//         senderOption = Some(expectActor)

//         expectActor ? Success

//         expectActor.stop

//         (fem ? TestGetIdents) onSuccess {
//             case Some(idents: SvdFileEventsManager#IdentsMap) => idents must beEmpty
//             case x => fail("Some fail: %s".format(x))
//         }
//     }


//         // TODO: Write (if possible) more stress tests

//     it should "not raise exception if SvdFileEventsManager is not started" in {
//         system.actorOf(Props(new SvdTestFileEventsReactor)).isExpectation
//     }

//     // TODO: this is sloooooooooooow
//     // "Stress test" should {
//     //     doBefore {
//     //         beforeExpectActor
//     //         try { FileUtils.forceDelete(DIR) } catch { case _ => }
//     //         fem = system.actorOf[TestSvdFileEventsManager].start
//     //     }
//     //
//     //     doAfter {
//     //         afterExpectActor
//     //         registry.shutdownAll
//     //     }
//     //
//     //     "get 100 modified files" in {
//     //         val range = (1 to 4)
//     //         val filename = (i: Int) => DIR + "/stress_" + i + "_mod"
//     //
//     //         val actors = range map { i =>
//     //             val name = filename(i)
//     //             touch(name)
//     //             name
//     //         } map { name =>
//     //             (name, system.actorOf(new SvdTestFileEventsReactorForFile(name)).start)
//     //         }
//     //
//     //         actors.foreach { case(_, actor) =>
//     //             expectActor = actor
//     //             senderOption = Some(expectActor)
//     //
//     //             expectActor ? Success
//     //         }
//     //
//     //         actors.foreach { case(name, _) =>
//     //             writeFile(name, "new content")
//     //         }
//     //
//     //         actors.foreach { case(name, actor) =>
//     //             println("expecting actor " + name)
//     //             expectActor = actor
//     //             senderOption = Some(expectActor)
//     //
//     //             expectActor ?* (SvdFileEvent(name, 0x04), SvdFileEvent(name, 0x06))
//     //         }
//     //     }
//     // }

// }
