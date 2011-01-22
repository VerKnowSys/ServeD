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

class TestSvdFileEventsReactor extends SvdExpectActor with SvdFileEventsReactor {
    registerFileEventFor("/tmp/served/file_events_test/single", Modified)
}

class SvdFileEventsManagerTest extends Specification with SvdExpectActorSpecification {
    final val DIR = "/tmp/served/file_events_test"
    
    var fw: ActorRef = null
    
    "SvdFileWatcher" should {
        doBefore { 
            beforeSvdExpectActor 
            fw = actorOf(new SvdFileWatcher(expectActor, "/path/to/file", 0x01 | 0x02)).start
        }
        
        doAfter { 
            afterSvdExpectActor
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
        
    "SvdFileEventsManager" should {
        doBefore { 
            beforeSvdExpectActor 
            try { FileUtils.forceDelete(DIR) } catch { case _ => }
            fem = actorOf[SvdFileEventsManager].start   
        }
        
        doAfter { 
            afterSvdExpectActor
            registry.shutdownAll 
        }
        
        "start SvdFileEventsManager withus SvdFileWatcher" in {
            registry.actorsFor[SvdFileEventsManager] must haveSize(1)
            registry.actorsFor[SvdFileWatcher] must haveSize(0)
        }
            
        "spawn new file watcher using explicit message" in {
            touch(DIR + "/single")
            
            fem ! RegisterFileEvent(DIR + "/single", 0x02)
            expectActor ? Success
            
            registry.actorsFor[SvdFileEventsManager] must haveSize(1)
            registry.actorsFor[SvdFileWatcher] must haveSize(1)            
        }
        
        "spawn new file watcher using SvdFileEventsReactor trait" in {
            touch(DIR + "/single")
            
            expectActor = actorOf[TestSvdFileEventsReactor].start
            senderOption = Some(expectActor)
            
            expectActor ? Success
        }
        
        "notify actors when file modified" in {
            touch(DIR + "/single")
            
            expectActor = actorOf[TestSvdFileEventsReactor].start
            senderOption = Some(expectActor)
            
            expectActor ? Success
            
            touch(DIR + "/single")
            
            expectActor ? FileEvent(DIR + "/single", 0x02)
        }
        
        // TODO: Write (if possible) more stress tests
    }
    
}
