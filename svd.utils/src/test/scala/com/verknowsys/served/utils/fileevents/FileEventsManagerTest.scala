package com.verknowsys.served.utils.fileevents

import com.verknowsys.served.SpecHelpers._
import com.verknowsys.served.spechelpers._
import com.verknowsys.served.utils.signals.Success
import org.specs.Specification
import java.io._
import org.apache.commons.io.FileUtils

import akka.actor._
import akka.actor.Actor._

import com.verknowsys.served.utils.fileevents.CLibrary._


class TestFileEventsReactor extends ExpectActor with FileEventsReactor {
    registerFileEventFor("/tmp/served/file_events_test/single", Modified)
}

class FileEventsManagerTest extends Specification with ExpectActorSpecification {
    final val DIR = "/tmp/served/file_events_test"
    
    var fw: ActorRef = null
    
    "FileWatcher" should {
        doBefore { 
            beforeExpectActor 
            fw = actorOf(new FileWatcher(expectActor, "/path/to/file", 0x01 | 0x02)).start
        }
        
        doAfter { 
            afterExpectActor
            registry.shutdownAll 
        }
        
        "forward message with matching flag" in {
            fw ! BareFileEvent("/path/to/file", 0x01)
            expectActor ? FileEvent("/path/to/file", 0x01)
        }
        
        "forward message with other matching flag" in {
            fw ! BareFileEvent("/path/to/file", 0x02)
            expectActor ? FileEvent("/path/to/file", 0x02)
        }
        
        "not forward message with not matching flag" in {
            fw ! BareFileEvent("/path/to/file", 0x04)
            expectActor ? nothing
        }

    }
    
    
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
        
        "start FileEventsManager withus FileWatcher" in {
            registry.actorsFor[FileEventsManager] must haveSize(1)
            registry.actorsFor[FileWatcher] must haveSize(0)
        }
            
        "spawn new file watcher using explicit message" in {
            touch(DIR + "/single")
            
            fem ! RegisterFileEvent(DIR + "/single", 0x02)
            expectActor ? Success
            
            registry.actorsFor[FileEventsManager] must haveSize(1)
            registry.actorsFor[FileWatcher] must haveSize(1)            
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
            
            touch(DIR + "/single")
            
            expectActor ? FileEvent(DIR + "/single", 0x02)
        }
        
        // TODO: Write (if possible) more stress tests
    }
    
}
