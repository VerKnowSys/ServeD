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
        registerFileEventFor("/tmp/served/file_events_test/single", Modified)
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
        }
        
        "notify actors when file modified" in {
            touch(DIR + "/single")
            
            expectActor = actorOf[SvdTestFileEventsReactor].start
            senderOption = Some(expectActor)
            
            expectActor ? Success
            
            writeFile(DIR + "/single", "new content")
            
            expectActor ?* (SvdFileEvent(DIR + "/single", 0x04), SvdFileEvent(DIR + "/single", 0x06))
        }
        
        "register few events" in {
            touch(DIR + "/single")
            actorOf[SvdTestFileEventsReactor].start
            actorOf[SvdTestFileEventsReactor].start
            actorOf[SvdTestFileEventsReactor].start.isExpectation
        }
        
        // "raise SvdFileOpenException exception" in {
        //     {
        //         expectActor = actorOf(new SvdExpectActor {
        //             fem ! SvdRegisterFileEvent(DIR + "/not-existing-at-all-123", 0x02, self)
        //         }).start
        //     } must throwA[SvdFileOpenException]
        // }
        // "unregister file event when stoped" in {
        //     touch(DIR + "/single")
        //     
        //     expectActor = actorOf[SvdTestFileEventsReactor].start
        //     senderOption = Some(expectActor)
        //     
        //     expectActor ? Success
        //     
        //     expectActor.stop
        //     
        //     // TODO: Test this!
        // }
    
        
        // TODO: Write (if possible) more stress tests
    }
    
    "SvdFileEventsReactor" should {
        "not raise exception if SvdFileEventsManager is not started" in {
            actorOf[SvdTestFileEventsReactor].start.isExpectation
        }
    }
    
}
