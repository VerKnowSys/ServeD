package com.verknowsys.served.systemmanager.storage

import org.specs._


class StorageTest extends Specification {
    var db: Storage = null
    
    "Storage" should {
        doBefore {
            db = new Storage("/tmp/storage_test.db")
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
            db.save(new ProcessInfo(120, "Foo", 10, 21))
            db.save(new ProcessInfo(120, "Foo", 5, 22))
            db.save(new ProcessInfo(120, "Foo", 4, 23))
            db.avgCpuByPID(120) must_== 5.0
        }
        
        // "average CPU by PID and time range" in {
        //     
        // }
        
        "calculate average CPU by name" in {
            db.save(new ProcessInfo(120, "Foo", 1, 20))
            db.save(new ProcessInfo(120, "Foo", 10, 21))
            db.save(new ProcessInfo(120, "Foo", 5, 22))
            db.save(new ProcessInfo(120, "Foo", 4, 23))
            db.avgCpuByName("Foo") must_== 5.0
        }
        
        // "average CPU by name and time range" in {
        //     
        // }
     }
}
