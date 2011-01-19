package com.verknowsys.served.utils.fileevents

import com.verknowsys.served.SpecHelpers._
import com.verknowsys.served.spechelpers.ExpectActor
import org.specs.Specification
import java.io._
import org.apache.commons.io.FileUtils

import akka.actor._
import akka.actor.Actor._

class FileEventsManagerTest extends Specification with ExpectActor {
    final val DIR = "/tmp/served/file_events_test"
    
    "FileWatcher" should {
        "send correct message to owner" in {
            val owner = expectActor
            val fw = actorOf(new FileWatcher(expectActor, "/path/to/file", 0x01 | 0x02)).start
            
            fw ! BareFileEvent("/path/to/file", 0x01)
            expectActor ? FileEvent("/path/to/file", 0x01)
            
            fw ! BareFileEvent("/path/to/file", 0x02)
            expectActor ? FileEvent("/path/to/file", 0x02)
            
            fw ! BareFileEvent("/path/to/file", 0x04)
            expectActor ? nothing
        }
    }
    
        
    "FileEventsManager" should {        
        doBefore { 
            // echoService = actorOf[EchoService].start
        }
        
        doAfter {
            // echoService.stop
        }
        
        "work" in {
            // echoService ! Dupa(1)
            // expectActor ?* (Dupa(2), Dupa(1), Dupa(3))
        }
    }
    
}
