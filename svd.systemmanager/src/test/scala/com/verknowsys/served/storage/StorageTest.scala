package com.verknowsys.served.systemmanager.storage

import org.specs._
import com.verknowsys.served.SvdSpecHelpers._


class StorageTest extends Specification {
    val now = new java.util.Date
    
    def time(sec: Int = 0) = new java.sql.Timestamp(now.getTime() + sec*1000);
     
    var db: Storage = null
    
    def reconnect {
        if(db != null) db.close
        db = new Storage("/tmp/served_tests/storage_test")
    }
    
    "Storage" should {
        doBefore {
            mkdir("/tmp/served_tests")
            reconnect
            
            db.save(new ProcessInfo(120, "Foo", 1, 20, time()))
            db.save(new ProcessInfo(120, "Foo", 10, 21, time(100)))
            db.save(new ProcessInfo(120, "Foo", 5, 22, time(200)))
            db.save(new ProcessInfo(120, "Foo", 8, 23, time(300)))
            db.save(new ProcessInfo(121, "Foo", 5, 50, time(200)))
            db.save(new ProcessInfo(122, "Foo", 4, 56, time(300)))
            db.save(new ProcessInfo(124, "Foo 2", 5, 51, time(100)))
            db.save(new ProcessInfo(126, "Bar", 5, 59, time(250)))
            
            reconnect
        }
        
        doAfter {
            db.close
            rmdir("/tmp/served_tests")
        }
        
        "calculate average CPU by PID" in {
            db.avgCpuByPID(120) must_== 6.0
        }
        
        "calculate average CPU by PID and time range" in {
            db.avgCpuByPIDAndTime(120, time(150), time(400)) must_== 6.5
        }
        
        "calculate average CPU by name" in {
            db.avgCpuByName("Foo") must_== 5.5
        }
        
        "calculate average CPU by name and time range" in {
            db.avgCpuByNameAndTime("Foo", time(150), time(400)) must_== 5.5
        }
        
        "calculate sum of CPU by PID" in {
            db.sumCpuByPID(120) must_== 24
        }
        
        "calculate sum of CPU by PID and time range" in {
            db.sumCpuByPIDAndTime(120, time(150), time(400)) must_== 13
        }
        
        "calculate sum of CPU by name" in {
            db.sumCpuByName("Foo") must_== 33
        }
        
        "calculate sum of CPU by name and time range" in {
            db.sumCpuByNameAndTime("Foo", time(150), time(400)) must_== 22
        }
        
        "calculate average memory by PID" in {
            db.avgMemByPID(120) must_== 21.5
        }
        
        "calculate average memory by PID and time range" in {
            db.avgMemByPIDAndTime(120, time(150), time(400)) must_== 22.5
        }
        
        "calculate average memory by name" in {
            db.avgMemByName("Foo") must_== 32
        }
        
        "calculate average memory by name and time range" in {
            db.avgMemByNameAndTime("Foo", time(150), time(400)) must_== 37.75
        }
        
        "calculate sum of memory by PID" in {
            db.sumMemByPID(120) must_== 86
        }
        
        "calculate sum of memory by PID and time range" in {
            db.sumMemByPIDAndTime(120, time(150), time(400)) must_== 45
        }
        
        "calculate sum of memory by name" in {
            db.sumMemByName("Foo") must_== 192
        }
        
        "calculate sum of memory by name and time range" in {
            db.sumMemByNameAndTime("Foo", time(150), time(400)) must_== 151
        }
    }
    
    "Storage buffering" should {
        "delay save" in {
            if(db != null) db.close
            rmdir("/tmp/served_tests")
            mkdir("/tmp/served_tests")
            reconnect
            
            db.getAll.size must_== 0
            db.getBufferSize must_== 0
            
            (1 to Storage.MAX_BUFFER_SIZE) foreach { i =>
                db.add(new ProcessInfo(i, "tmp", 10, 20))
                db.getAll.size must_== 0
                db.getBufferSize must_== i
            }
            
            db.add(new ProcessInfo(0, "tmp", 10, 20))
            db.getAll.size must_== Storage.MAX_BUFFER_SIZE
            db.getBufferSize must_== 1
        }
    }
}
