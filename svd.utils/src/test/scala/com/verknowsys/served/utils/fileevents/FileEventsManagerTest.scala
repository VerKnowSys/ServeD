package com.verknowsys.served.utils.fileevents

import com.verknowsys.served.SpecHelpers._
import com.verknowsys.served.spechelpers.ExpectActor
import com.verknowsys.served.utils.signals.Success
import org.specs.Specification
import java.io._
import org.apache.commons.io.FileUtils

import akka.actor._
import akka.actor.Actor._


// class TestFileEventsManager extends FileEventsManager {
//     def getIdents = idents
// }

class FileEventsManagerTest extends Specification with ExpectActor {
    final val DIR = "/tmp/served/file_events_test"
    
    "FileWatcher" should {
        doBefore { beforeExpectActor }
        doAfter { afterExpectActor }
        
        "forward only message that matches flag" in {
            val fw = actorOf(new FileWatcher(expectActor, "/path/to/file", 0x01 | 0x02)).start
            
            fw ! BareFileEvent("/path/to/file", 0x01)
            expectActor ? FileEvent("/path/to/file", 0x01)
            
            fw ! BareFileEvent("/path/to/file", 0x02)
            expectActor ? FileEvent("/path/to/file", 0x02)
            
            fw ! BareFileEvent("/path/to/file", 0x04)
            expectActor ? nothing
            
            registry.shutdownAll
        }
    }
    
        
    "FileEventsManager" should {
        doBefore { beforeExpectActor }
        doAfter { afterExpectActor }
            
        "spawn new file watcher" in {
            try { FileUtils.forceDelete(DIR) } catch { case _ => }
            touch(DIR + "/single")
            
            val fem = actorOf[FileEventsManager].start
            registry.actorsFor[FileEventsManager] must haveSize(1)
            registry.actorsFor[FileWatcher] must haveSize(0)
            
            fem ! RegisterFileEvent(DIR + "/single", 0x02)
            expectActor ? Success
            
            registry.actorsFor[FileEventsManager] must haveSize(1)
            registry.actorsFor[FileWatcher] must haveSize(1)
            
            registry.shutdownAll
        }
    }
    
}
