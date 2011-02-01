package com.verknowsys.served.utils

import events._
import com.verknowsys.served.utils.events._
import com.verknowsys.served.SvdSpecHelpers._
import com.verknowsys.served.spechelpers._
import com.verknowsys.served.utils.signals.Success
import org.specs.Specification
import java.io._
import org.apache.commons.io.FileUtils

import akka.actor._
import akka.actor.Actor._

class SvdTestFileEventsReactor extends SvdExpectActor with SvdFileEventsReactor {
    override def preStart {
        new File("/tmp/served/file_events_test").mkdir
        registerFileEventFor("/tmp/served/file_events_test/single", Modified)
    }
}

class SvdTestFileEventsReactorForFile(path: String) extends SvdExpectActor with SvdFileEventsReactor {
    override def preStart {
        registerFileEventFor(path, Modified)
    }
}

case object TestGetIdents

class TestSvdFileEventsManager extends SvdFileEventsManager {
    override def receive = super.receive orElse {
        case TestGetIdents => self reply idents
    }
}

class SvdFileEventsManagerTest extends Specification with SvdExpectActorSpecification {
    final val DIR = "/tmp/served/file_events_test"
    new File(DIR).mkdir
    
    var fem: ActorRef = null
        
    "SvdFileEventsManager as actor" should {
        doBefore { 
            beforeExpectActor 
            try { FileUtils.forceDelete(DIR) } catch { case _ => }
            fem = actorOf[TestSvdFileEventsManager].start
        }
        
        doAfter { 
            afterExpectActor
            registry.shutdownAll 
        }
        
        "start FileEventsManager" in {
            registry.actorsFor[TestSvdFileEventsManager] must haveSize(1)
        }
        

        "register new file event using explicit message" in {
            touch(DIR + "/single")
            
            expectActor = actorOf(new SvdExpectActor {
                fem ! SvdRegisterFileEvent(DIR + "/single", 0x02, self)
            }).start
            senderOption = Some(expectActor)
            
            expectActor ? Success
            
            (fem !! TestGetIdents) match {
                case Some(idents: SvdFileEventsManager#IdentsMap) => idents.mapValues { case (a,b) => (a, b.toList) } must haveValue( (DIR + "/single", (0x02, expectActor) :: Nil))
                case _ => fail("timeout")
            }

        }
        
        "register new file event using SvdFileEventsReactor trait" in {
            touch(DIR + "/single")
            
            expectActor = actorOf[SvdTestFileEventsReactor].start
            senderOption = Some(expectActor)
            
            expectActor ? Success
            
            (fem !! TestGetIdents) match {
                case Some(idents: SvdFileEventsManager#IdentsMap) => idents.mapValues { case (a,b) => (a, b.toList) } must haveValue( (DIR + "/single", (Modified, expectActor) :: Nil))
                case _ => fail("timeout")
            }
        }
        
        "notify actors when file modified" in {
            touch(DIR + "/single")
            
            expectActor = actorOf[SvdTestFileEventsReactor].start
            senderOption = Some(expectActor)
            
            expectActor ? Success
            
            writeFile(DIR + "/single", "new content")
            
            expectActor ?* (SvdFileEvent(DIR + "/single", 0x04), SvdFileEvent(DIR + "/single", 0x06))
        }
        
        "register few events for the same file" in {
            touch(DIR + "/single")
            actorOf[SvdTestFileEventsReactor].start
            actorOf[SvdTestFileEventsReactor].start
            expectActor = actorOf[SvdTestFileEventsReactor].start
            senderOption = Some(expectActor)
            
            expectActor ? Success
            
            (fem !! TestGetIdents) match {
                case Some(idents: SvdFileEventsManager#IdentsMap) => 
                    idents must haveSize(1)
                    idents.toList.head._2._2 must haveSize(3)
                case _ => fail("timeout")
            }
        }
        
        "register few events for different files" in {
            touch(DIR + "/one")
            touch(DIR + "/two")
            touch(DIR + "/three")
            touch(DIR + "/four")
            
            actorOf(new SvdTestFileEventsReactorForFile(DIR + "/one")).start
            actorOf(new SvdTestFileEventsReactorForFile(DIR + "/two")).start
            actorOf(new SvdTestFileEventsReactorForFile(DIR + "/three")).start
            expectActor = actorOf(new SvdTestFileEventsReactorForFile(DIR + "/four")).start
            senderOption = Some(expectActor)
            
            expectActor ? Success
            
            (fem !! TestGetIdents) match {
                case Some(idents: SvdFileEventsManager#IdentsMap) => 
                    idents must haveSize(4)
                    idents.toList(0)._2._2 must haveSize(1)
                    idents.toList(1)._2._2 must haveSize(1)
                    idents.toList(2)._2._2 must haveSize(1)
                    idents.toList(3)._2._2 must haveSize(1)
                case _ => fail("timeout")
            }
        }
        
        "unregister events when stopped" in {
            touch(DIR + "/single")
            
            expectActor = actorOf[SvdTestFileEventsReactor].start
            senderOption = Some(expectActor)
            
            expectActor ? Success
            
            expectActor.stop
            
            (fem !! TestGetIdents) match {
                case Some(idents: SvdFileEventsManager#IdentsMap) => idents must beEmpty
                case _ => fail("timeout")
            }
        }

        // TODO: How to test throwing exceptions in other threads?
        // "raise SvdFileOpenException exception" in {
        //     {
        //         expectActor = actorOf(new SvdExpectActor {
        //             fem ! SvdRegisterFileEvent(DIR + "/not-existing-at-all-123", 0x02, self)
        //         }).start
        //     } must throwA[SvdFileOpenException]
        // }
        
        "unregister file event when stoped" in {
            touch(DIR + "/single")
            
            expectActor = actorOf[SvdTestFileEventsReactor].start
            senderOption = Some(expectActor)
            
            expectActor ? Success
            
            expectActor.stop
            
            (fem !! TestGetIdents) match {
                case Some(idents: SvdFileEventsManager#IdentsMap) => idents must beEmpty
                case _ => fail("timeout")
            }
        }
    
        
        // TODO: Write (if possible) more stress tests
    }
    
    "SvdFileEventsReactor" should {
        "not raise exception if SvdFileEventsManager is not started" in {
            actorOf[SvdTestFileEventsReactor].start.isExpectation
        }
    }
    
    // TODO: this is sloooooooooooow
    // "Stress test" should {
    //     doBefore { 
    //         beforeExpectActor 
    //         try { FileUtils.forceDelete(DIR) } catch { case _ => }
    //         fem = actorOf[TestSvdFileEventsManager].start
    //     }
    //     
    //     doAfter { 
    //         afterExpectActor
    //         registry.shutdownAll 
    //     }
    //     
    //     "get 100 modified files" in {
    //         val range = (1 to 4)
    //         val filename = (i: Int) => DIR + "/stress_" + i + "_mod"
    //         
    //         val actors = range map { i =>
    //             val name = filename(i)
    //             touch(name)
    //             name
    //         } map { name => 
    //             (name, actorOf(new SvdTestFileEventsReactorForFile(name)).start) 
    //         }
    //         
    //         actors.foreach { case(_, actor) => 
    //             expectActor = actor
    //             senderOption = Some(expectActor)
    //     
    //             expectActor ? Success
    //         }
    //     
    //         actors.foreach { case(name, _) =>
    //             writeFile(name, "new content")
    //         }
    //             
    //         actors.foreach { case(name, actor) => 
    //             println("expecting actor " + name)
    //             expectActor = actor
    //             senderOption = Some(expectActor)
    //     
    //             expectActor ?* (SvdFileEvent(name, 0x04), SvdFileEvent(name, 0x06))
    //         }
    //     }
    // }
    
}
