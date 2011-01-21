package com.verknowsys.served.utils

import com.verknowsys.served.utils.events._
import com.verknowsys.served.SpecHelpers._
import com.verknowsys.served.spechelpers._
import com.verknowsys.served.utils.signals.Success
import org.specs.Specification
import java.io._
import org.apache.commons.io.FileUtils

import akka.actor._
import akka.actor.Actor._

object TestReactor extends ExpectActor with FileEventsReactor
import TestReactor._

class TestFileEventsReactor extends ExpectActor with FileEventsReactor {
    registerFileEventFor("/tmp/served/file_events_test/single", Modified)
}


class FileEventsManagerTest extends Specification with ExpectActorSpecification {
    final val DIR = "/tmp/served/file_events_test"
    
    var fem: ActorRef = null
        
    "FileEventsManager" should {
        doBefore { 
            beforeExpectActor 
            try { FileUtils.forceDelete(DIR) } catch { case _ => }
            fem = actorOf[FileEventsManager].start   
        }
        
        doAfter { 
            afterExpectActor
            registry.shutdownAll 
        }
        
        "start FileEventsManager" in {
            registry.actorsFor[FileEventsManager] must haveSize(1)
        }
            
        "register new file event using explicit message" in {
            touch(DIR + "/single")
            
            expectActor = actorOf(new ExpectActor {
                fem ! RegisterFileEvent(DIR + "/single", 0x02, self)
            }).start
            senderOption = Some(expectActor)
            
            expectActor ? Success
        }
        
        "spawn new file watcher using FileEventsReactor trait" in {
            touch(DIR + "/single")
            
            expectActor = actorOf[TestFileEventsReactor].start
            senderOption = Some(expectActor)
            
            expectActor ? Success
        }
        
        "notify actors when file modified" in {
            touch(DIR + "/single")
            
            expectActor = actorOf[TestFileEventsReactor].start
            senderOption = Some(expectActor)
            
            expectActor ? Success
            
            writeFile(DIR + "/single", "new content")
            
            expectActor ?* (FileEvent(DIR + "/single", 0x04), FileEvent(DIR + "/single", 0x06))
        }
        
        // TODO: Write (if possible) more stress tests
    }
    
}
