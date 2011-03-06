package com.verknowsys.served.db

import java.util.UUID
import com.mongodb.casbah.Imports._
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
            db.current.head("name") must_== "x1"
            db(uuid1).get("name") must_== "x1"
            
            db << DBObj(uuid2, "name" -> "y1")
            db.current.count must_== 2
            db.history.count must_== 0
            db(uuid1).get("name") must_== "x1"
            db(uuid2).get("name") must_== "y1"
            
            db << DBObj(uuid1, "name" -> "x2")
            db.current.count must_== 2
            db.history.count must_== 1
            db(uuid1).get("name") must_== "x2"
            db(uuid2).get("name") must_== "y1"
            
            db.historyFor(uuid1) must haveSize(1)
            db.historyFor(uuid1).head("name") must_== "x1"
            
            // val dbobjoption = db.history(uuid1)
            //             val dbobj = dbobjoption.get
            //             val dblistoption = dbobj.getAs[BasicDBList]("history")
            //             val dblist = dblistoption.get.map(_.asInstanceOf[DBObject])
            //             val first: DBObject = dblist.head
            //             val name = first("name")
            //             name must_== "x1"
            // .head.asDBObject("name") //map(_.asDBObject)
            
            
            // val obj = db.history(uuid1).get.getAs[BasicDBList]("history").get.head.asDBObject("name") //map(_.asDBObject)
            // println(obj)
            // 
            // ("name") must_== "x1"
            
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
