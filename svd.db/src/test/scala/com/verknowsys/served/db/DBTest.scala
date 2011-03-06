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


            db(uuid1) = ("name" -> "x1")
            db.current.count must_== 1
            db.history.count must_== 0
            db.current.head("name") must_== "x1"
            db(uuid1).get("name") must_== "x1"
            db.historyFor(uuid1) must beEmpty
            db.historyFor(uuid2) must beEmpty
            
            
            db(uuid2) = ("name" -> "y1")
            db.current.count must_== 2
            db.history.count must_== 0
            db(uuid1).get("name") must_== "x1"
            db(uuid2).get("name") must_== "y1"
            db.historyFor(uuid1) must beEmpty
            db.historyFor(uuid2) must beEmpty
            
            
            db(uuid1) = ("name" -> "x2")
            db.current.count must_== 2
            db.history.count must_== 1
            db(uuid1).get("name") must_== "x2"
            db(uuid2).get("name") must_== "y1"
            db.historyFor(uuid1) must haveSize(1)
            db.historyFor(uuid1).head("name") must_== "x1"
            db.historyFor(uuid2) must beEmpty
            
            
            db(uuid1) = ("name" -> "x3")
            db.current.count must_== 2
            db.history.count must_== 1
            db(uuid1).get("name") must_== "x3"
            db(uuid2).get("name") must_== "y1"
            db.historyFor(uuid1) must haveSize(2)
            db.historyFor(uuid1).map(_("name")) must_== List("x2", "x1")
            db.historyFor(uuid2) must beEmpty
            
            
            db(uuid1) = ("name" -> "x4")
            db.current.count must_== 2
            db.history.count must_== 1
            db.historyFor(uuid1) must haveSize(3)
            db.historyFor(uuid1).map(_("name")) must_== List("x3", "x2", "x1")
            db.historyFor(uuid2) must beEmpty
            
            db(uuid2) = ("name" -> "y2")
            db.current.count must_== 2
            db.history.count must_== 2
            db.historyFor(uuid1) must haveSize(3)
            db.historyFor(uuid1).map(_("name")) must_== List("x3", "x2", "x1")
            db.historyFor(uuid2) must haveSize(1)
            db.historyFor(uuid2).map(_("name")) must_== List("y1")

            db.close
        }
    }
}
