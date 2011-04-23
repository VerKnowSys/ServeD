package com.verknowsys.served.db

import org.specs._
import com.verknowsys.served.SvdSpecHelpers._
import java.util.UUID

case class User(name: String, uuid: UUID = UUID.randomUUID) extends DBObject(uuid)

class DBTest extends Specification {
    var server: DBServer = null
    var db: DBClient = null

    "DB" should {
        doBefore {
            rmdir("/tmp/svd_db_test")
            mkdir("/tmp/svd_db_test")
            server = new DBServer(9000, "/tmp/svd_db_test/dbservertest")
            db = server.openClient
        }
        
        doAfter {
            server.close
        }
        
        "store Users" in {
            val teamon = User("teamon")
            val dmilith = User("dmilith")
            
            db << teamon
            db << dmilith
            
            db.count[User].intValue must_== 2
            db.current.count[User].intValue must_== 2
            db.history.count[User].intValue must_== 0
        }
    }
    
}

//     var db: DB = null
//     
//     "DB" should {
//         
//         doBefore {
//             db = new DB
//             db.drop
//         }
//         
//         doAfter {
//             db.close
//         }
        
        // "just store some data" in {
        //     val uuid = UUID.randomUUID
        //     db(uuid) must_== None
        //     db(uuid) = ("name" -> "teamon")
        //     db(uuid).get("name") must_== "teamon"
        //     
        //     db.close
        //     db = new DB
        //     db(uuid).get("name") must_== "teamon"
        // }
        // 
        // "work" in {
        //     val uuid1 = UUID.randomUUID
        //     val uuid2 = UUID.randomUUID
        // 
        // 
        //     db(uuid1) = ("name" -> "x1")
        //     db.current.count must_== 1
        //     db.history.count must_== 0
        //     db.current.head("name") must_== "x1"
        //     db(uuid1).get("name") must_== "x1"
        //     db.historyFor(uuid1) must beEmpty
        //     db.historyFor(uuid2) must beEmpty
        //     
        //     
        //     db(uuid2) = ("name" -> "y1")
        //     db.current.count must_== 2
        //     db.history.count must_== 0
        //     db(uuid1).get("name") must_== "x1"
        //     db(uuid2).get("name") must_== "y1"
        //     db.historyFor(uuid1) must beEmpty
        //     db.historyFor(uuid2) must beEmpty
        //     
        //     
        //     db(uuid1) = ("name" -> "x2")
        //     db.current.count must_== 2
        //     db.history.count must_== 1
        //     db(uuid1).get("name") must_== "x2"
        //     db(uuid2).get("name") must_== "y1"
        //     db.historyFor(uuid1) must haveSize(1)
        //     db.historyFor(uuid1).head("name") must_== "x1"
        //     db.historyFor(uuid2) must beEmpty
        //     
        //     
        //     db(uuid1) = ("name" -> "x3")
        //     db.current.count must_== 2
        //     db.history.count must_== 1
        //     db(uuid1).get("name") must_== "x3"
        //     db(uuid2).get("name") must_== "y1"
        //     db.historyFor(uuid1) must haveSize(2)
        //     db.historyFor(uuid1).map(_("name")) must_== List("x2", "x1")
        //     db.historyFor(uuid2) must beEmpty
        //     
        //     
        //     db(uuid1) = ("name" -> "x4")
        //     db.current.count must_== 2
        //     db.history.count must_== 1
        //     db.historyFor(uuid1) must haveSize(3)
        //     db.historyFor(uuid1).map(_("name")) must_== List("x3", "x2", "x1")
        //     db.historyFor(uuid2) must beEmpty
        //     
        //     db(uuid2) = ("name" -> "y2")
        //     db.current.count must_== 2
        //     db.history.count must_== 2
        //     db.historyFor(uuid1) must haveSize(3)
        //     db.historyFor(uuid1).map(_("name")) must_== List("x3", "x2", "x1")
        //     db.historyFor(uuid2) must haveSize(1)
        //     db.historyFor(uuid2).map(_("name")) must_== List("y1")
        // }
        // 
        // "should not duplicated data" in {
        //     val uuid = UUID.randomUUID
        //     
        //     db(uuid) = ("name" -> "foo")
        //     db.current.count must_== 1
        //     db.history.count must_== 0
        //     
        //     db(uuid) = ("name" -> "foo")
        //     db.current.count must_== 1
        //     db.history.count must_== 0
        //     
        //     db(uuid) = ("name" -> "bar")
        //     db.current.count must_== 1
        //     db.history.count must_== 1
        //     
        //     db(uuid) = ("name" -> "baz")
        //     db(uuid) = ("name" -> "baz")
        //     db(uuid) = ("name" -> "xxx")
        //     db(uuid) = ("name" -> "xxx")
        //     db(uuid) = ("name" -> "xxx")
        //     db(uuid) = ("name" -> "xxx")
        //     db(uuid) = ("name" -> "yyy")
        //     
        //     db.current.count must_== 1
        //     db.history.count must_== 1
        //     db.historyFor(uuid).map(_("name")) must_== List("xxx", "baz", "bar", "foo")
        // }
    // }
// }
