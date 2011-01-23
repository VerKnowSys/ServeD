package com.verknowsys.served.utils

import com.verknowsys.served.utils.events._
import com.verknowsys.served.SvdSpecHelpers._
import com.verknowsys.served.spechelpers._
import com.verknowsys.served.utils.signals.Success
import org.specs.Specification
import java.io._
import org.apache.commons.io.FileUtils

import akka.actor._
import akka.actor.Actor._

object SvdTestReactor extends SvdExpectActor with SvdFileEventsReactor
import SvdTestReactor._

class SvdTestFileEventsReactor extends SvdExpectActor with SvdFileEventsReactor {
    override def preStart {
        registerFileEventFor("/tmp/served/file_events_test/single", Modified)
    }
}


class SvdFileEventsManagerTest extends Specification with SvdExpectActorSpecification {
    final val DIR = "/tmp/served/file_events_test"
    
    var fem: ActorRef = null
        
    "SvdFileEventsManager" should {
        doBefore { 
            beforeExpectActor 
            try { FileUtils.forceDelete(DIR) } catch { case _ => }
            fem = actorOf[SvdFileEventsManager].start   
        }
        
        doAfter { 
            afterExpectActor
            registry.shutdownAll 
        }
        
        "start FileEventsManager" in {
            registry.actorsFor[SvdFileEventsManager] must haveSize(1)
        }
            
        "register new file event using explicit message" in {
            touch(DIR + "/single")
            
            expectActor = actorOf(new SvdExpectActor {
                fem ! SvdRegisterFileEvent(DIR + "/single", 0x02, self)
            }).start
            senderOption = Some(expectActor)
            
            expectActor ? Success
        }
        
        "spawn new file watcher using SvdFileEventsReactor trait" in {
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
        
        // TODO: Write (if possible) more stress tests
    }
    
}
