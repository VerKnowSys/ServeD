package com.verknowsys.served.db

import org.specs._
import com.verknowsys.served.SvdSpecHelpers._
import java.util.UUID

import org.neodatis.odb.core.query.nq.SimpleNativeQuery
import org.neodatis.odb.core.query.nq.NativeQuery




case class User(val name: String, uuid: UUID = UUID.randomUUID) extends DBObject(uuid)
object Users extends DBManager[User]

case class Drug(val name: String, uuid: UUID = UUID.randomUUID) extends DBObject(uuid)
object Drugs extends DBManager[Drug]

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
            val teamon = new User("teamon")
            val dmilith = new User("dmilith")
            
            db << teamon
            db << dmilith
            
            Users(db).count.intValue must_== 2
            Users(db.current).count.intValue must_== 2
            Users(db.history).count.intValue must_== 0
            
            
            val users1 = Users(db).all
            users1 must haveSize(2)
            users1 must contain(teamon)
            users1 must contain(dmilith)
            
            val users2 = Users(db.current).all
            users2 must haveSize(2)
            users2 must contain(teamon)
            users2 must contain(dmilith)
            
            val users3 = Users(db.history).all
            users3 must haveSize(0)
        }
        
        "store Users and Drugs" in {
            val teamon  = new User("teamon")
            val dmilith = new User("dmilith")
            val cig     = new Drug("cig")
            val joint   = new Drug("joint")
            
            db << teamon
            db << dmilith
            db << cig
            db << joint
            
            val users1 = Users(db).all
            users1 must haveSize(2)
            users1 must contain(teamon)
            users1 must contain(dmilith)
            
            val drugs1 = Drugs(db).all
            drugs1 must haveSize(2)
            drugs1 must contain(cig)
            drugs1 must contain(joint)
            
            val users2 = Users(db.history).all
            users2 must haveSize(0)
            
            val drugs2 = Drugs(db.history).all
            drugs2 must haveSize(0)
        }
        
        "query by predicate" in {
            val teamon = new User("teamon")
            val dmilith = new User("dmilith")
            val lopex = new User("lopex")
            
            db << teamon
            db << dmilith
            db << lopex
            db << Drug("a")
            db << Drug("b")
            db << Drug("c")
            
            val users1 = Users(db)(e => true)
            users1 must haveSize(3)
            users1 must contain(teamon)
            users1 must contain(dmilith)
            users1 must contain(lopex)
            
            val users2 = Users(db)(_.name == "teamon")
            users2 must haveSize(1)
            users2 must contain(teamon)
            
            val users3 = Users(db){ e => e.name == "teamon" || e.name == "dmilith" }
            users3 must haveSize(2)
            users3 must contain(teamon)
            users3 must contain(dmilith)
            
            val users4 = Users(db)(_.name == "lopex")
            users4 must haveSize(1)
            users4 must contain(lopex)
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
