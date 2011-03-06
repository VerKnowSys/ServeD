package com.verknowsys.served.db

import java.util.UUID
import org.specs._

class DBTest extends Specification {
    "DB" should {
        "work" in {
            val db = new DB
            db.drop

            val uuid1 = UUID.randomUUID
            val uuid2 = UUID.randomUUID

            db << DBObj(uuid1, "name" -> "x1")
            db.current.count must_== 1
            db.history.count must_== 0
            
            db << DBObj(uuid2, "name" -> "y1")
            db.current.count must_== 2
            db.history.count must_== 0
            
            db << DBObj(uuid1, "name" -> "x1")
            db.current.count must_== 2
            db.history.count must_== 1
            
            db << DBObj(uuid1, "name" -> "x3")
            db.current.count must_== 2
            db.history.count must_== 1
            
            db << DBObj(uuid1, "name" -> "x4")
            db.current.count must_== 2
            db.history.count must_== 1
            
            db << DBObj(uuid2, "name" -> "y2")
            db.current.count must_== 2
            db.history.count must_== 2

            db.close
        }
    }
}
