package com.verknowsys.served.systemmanager.storage

import com.verknowsys.served.testing._
import com.verknowsys.served.SvdSpecHelpers._
import com.verknowsys.served.utils._

class StorageBufferingTest extends DefaultTest {
    it should "delay save" in {
        val path = randomPath
        mkdir(path)
        val db = new Storage(path / "db")
        
        db.getAll.size should equal (0)
        db.getBufferSize should equal (0)
                
        (1 to Storage.MAX_BUFFER_SIZE) foreach { i =>
            db.add(new ProcessInfo(i, "tmp", 10, 20))
            db.getAll.size should equal (0)
            db.getBufferSize should equal (i)
        }
        
        db.add(new ProcessInfo(0, "tmp", 10, 20))
        db.getAll.size should equal (Storage.MAX_BUFFER_SIZE)
        db.getBufferSize should equal (1)
        
        // cleanup
        db.close
        rmdir(path)
    }
}

class StorageTest extends DefaultTest {
    def time(sec: Int = 0) = new java.sql.Timestamp(rightNow.getTime() + sec*1000)

    def reconnect {
        if(db != null) db.close
        db = new Storage(path / "db")
    }
    
    override def afterEach {
        db.close
        rmdir(path)
    }

    val rightNow = new java.util.Date
    var db: Storage = null
    val path = randomPath
    mkdir(path)

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

    it should "calculate average CPU by PID" in {
        db.avgCpuByPID(120) should equal (6.0)
    }
    
    it should "calculate average CPU by PID and time range" in {
        db.avgCpuByPIDAndTime(120, time(150), time(400)) should equal (6.5)
    }
    
    it should "calculate average CPU by name" in {
        db.avgCpuByName("Foo") should equal (5.5)
    }
    
    it should "calculate average CPU by name and time range" in {
        db.avgCpuByNameAndTime("Foo", time(150), time(400)) should equal (5.5)
    }
    
    it should "calculate sum of CPU by PID" in {
        db.sumCpuByPID(120) should equal (24)
    }
    
    it should "calculate sum of CPU by PID and time range" in {
        db.sumCpuByPIDAndTime(120, time(150), time(400)) should equal (13)
    }
    
    it should "calculate sum of CPU by name" in {
        db.sumCpuByName("Foo") should equal (33)
    }
    
    it should "calculate sum of CPU by name and time range" in {
        db.sumCpuByNameAndTime("Foo", time(150), time(400)) should equal (22)
    }
    
    it should "calculate average memory by PID" in {
        db.avgMemByPID(120) should equal (21.5)
    }
    
    it should "calculate average memory by PID and time range" in {
        db.avgMemByPIDAndTime(120, time(150), time(400)) should equal (22.5)
    }
    
    it should "calculate average memory by name" in {
        db.avgMemByName("Foo") should equal (32)
    }
    
    it should "calculate average memory by name and time range" in {
        db.avgMemByNameAndTime("Foo", time(150), time(400)) should equal (37.75)
    }
    
    it should "calculate sum of memory by PID" in {
        db.sumMemByPID(120) should equal (86)
    }
    
    it should "calculate sum of memory by PID and time range" in {
        db.sumMemByPIDAndTime(120, time(150), time(400)) should equal (45)
    }
    
    it should "calculate sum of memory by name" in {
        db.sumMemByName("Foo") should equal (192)
    }
    
    it should "calculate sum of memory by name and time range" in {
        db.sumMemByNameAndTime("Foo", time(150), time(400)) should equal (151)
    }
}
