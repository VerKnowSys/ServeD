package com.verknowsys.served.systemmanager.storage

import org.specs._
import com.verknowsys.served.SvdSpecHelpers._


class StorageTest extends Specification {
    val now = new java.util.Date
    
    def time(sec: Int = 0) = new java.sql.Timestamp(now.getYear(), now.getMonth(), now.getDay(), now.getHours(), now.getMinutes(), sec, 0);
     
    var db: Storage = null
    
    def reconnect {
        if(db != null) db.close
        db = new Storage("/tmp/served_tests/storage_test")
    }
    
    "Storage" should {
        doBefore {
            mkdir("/tmp/served_tests")
            reconnect
        }
        
        doAfter {
            db.close
            rmdir("/tmp/served_tests")
        }
        
        // "create table if file do not exists" in {
        //     
        //     val pi = new ProcessInfo(120, "Foo", 1, 2)
        //     db.store(pi)
        //     db.getBy
        //     
        //     
        // }
        
        "calculate average CPU by PID" in {
            db.save(new ProcessInfo(120, "Foo", 1, 20))
            db.save(new ProcessInfo(120, "Foo", 6, 21))
            db.save(new ProcessInfo(120, "Foo", 5, 22))
            db.save(new ProcessInfo(120, "Foo", 4, 23))
            db.save(new ProcessInfo(121, "Foo", 5, 50))
            db.save(new ProcessInfo(122, "Foo", 4, 40))
            reconnect
            db.avgCpuByPID(120) must_== 4.0
        }
        
        "calculate average CPU by PID and time range" in {
            db.save(new ProcessInfo(120, "Foo", 1, 20, time()))
            db.save(new ProcessInfo(120, "Foo", 10, 21, time(100)))
            db.save(new ProcessInfo(121, "Foo", 5, 50, time(100)))
            db.save(new ProcessInfo(120, "Foo", 5, 22, time(200)))
            db.save(new ProcessInfo(122, "Foo", 4, 40, time(200)))
            db.save(new ProcessInfo(120, "Foo", 8, 23, time(300)))
            reconnect
            db.avgCpuByPIDAndTime(120, time(150), time(400)) must_== 6.5
        }
        
        // "calculate average CPU by name" in {
        //     db.save(new ProcessInfo(120, "Foo", 1, 20))
        //     db.save(new ProcessInfo(120, "Foo", 10, 21))
        //     db.save(new ProcessInfo(120, "Foo", 5, 22))
        //     db.save(new ProcessInfo(120, "Foo", 4, 23))
        //     db.save(new ProcessInfo(120, "Bar", 5, 52))
        //     db.save(new ProcessInfo(120, "Foo 2", 4, 53))
        //     reconnect
        //     db.avgCpuByName("Foo") must_== 5.0
        // }
        
        // "average CPU by name and time range" in {
        //     
        // }
        
        // "calculate sum of memory by PID" in {
        //     db.save(new ProcessInfo(120, "Foo", 1, 20))
        //     db.save(new ProcessInfo(120, "Foo", 10, 21))
        //     db.save(new ProcessInfo(120, "Foo", 5, 22))
        //     db.save(new ProcessInfo(120, "Foo", 4, 23))
        //     db.save(new ProcessInfo(121, "Foo", 5, 50))
        //     db.save(new ProcessInfo(122, "Foo", 4, 40))
        //     reconnect
        //     db.sumMemByPID(120) must_== 86
        // }
        
        // "average CPU by PID and time range" in {
        //     
        // }
        
        // "calculate sum of memory by name" in {
        //     db.save(new ProcessInfo(120, "Foo", 1, 20))
        //     db.save(new ProcessInfo(120, "Foo", 10, 21))
        //     db.save(new ProcessInfo(120, "Foo", 5, 22))
        //     db.save(new ProcessInfo(120, "Foo", 4, 23))
        //     db.save(new ProcessInfo(120, "Bar", 5, 52))
        //     db.save(new ProcessInfo(120, "Foo 2", 4, 53))
        //     reconnect
        //     db.sumMemByName("Foo") must_== 86
        // }
     }
}
