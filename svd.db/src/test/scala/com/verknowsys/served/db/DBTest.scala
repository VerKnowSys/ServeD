package com.verknowsys.served.db

import org.specs._
import com.verknowsys.served.SvdSpecHelpers._

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
        
        "Data model" in {
            val teamon = new User("teamon")
            val teamon2 = teamon.copy()
            teamon.uuid must_== teamon2.uuid
        }
        
        "store Users" in {
            val teamon = new User("teamon")
            val dmilith = new User("dmilith")
            
            db << teamon
            db << dmilith
            
            Users(db).count must_== 2
            
            
            val users1 = Users(db)
            users1 must haveSize(2)
            users1 must contain(teamon)
            users1 must contain(dmilith)
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
            
            val users1 = Users(db)
            users1 must haveSize(2)
            users1 must contain(teamon)
            users1 must contain(dmilith)
            
            val drugs1 = Drugs(db)
            drugs1 must haveSize(2)
            drugs1 must contain(cig)
            drugs1 must contain(joint)
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
        
        "query by uuid" in {
            val teamon  = User("teamon")
            val dmilith = User("dmilith")
            
            val uuid1 = teamon.uuid
            val uuid2 = dmilith.uuid
            val uuid3 = randomUUID
            
            db << teamon
            db << dmilith
            
            Users(db)(uuid1) must beSome(teamon)
            Users(db)(uuid2) must beSome(dmilith)
            Users(db)(uuid3) must beNone
        }
        
        "use history" in {
            val teamon  = User("teamon")
            val dmilith = User("dmilith")
            
            val uuid1 = teamon.uuid
            val uuid2 = dmilith.uuid
            val uuid3 = randomUUID
            
            db << teamon
            db << dmilith
            
            Users(db) must haveSize(2)
            Users(db).historyFor(uuid1) must beEmpty
            Users(db).historyFor(uuid2) must beEmpty
            Users(db).historyFor(uuid3) must beEmpty
            
            val teamon2 = teamon.copy(name = "teamon2")
            val teamon3 = teamon.copy(name = "teamon3")
            val dmilith2 = dmilith.copy(name = "dmilith2")
            
            db << teamon2
            
            Users(db) must haveSize(2)
            Users(db)(uuid1) must beSome(teamon2)
            Users(db)(uuid2) must beSome(dmilith)
            Users(db)(uuid3) must beNone
            Users(db).historyFor(uuid1) must haveSize(1)
            Users(db).historyFor(uuid1) must contain(teamon)
            Users(db).historyFor(uuid2) must beEmpty
            Users(db).historyFor(uuid3) must beEmpty
            
            
            db << teamon3
            
            Users(db) must haveSize(2)
            Users(db)(uuid1) must beSome(teamon3)
            Users(db)(uuid2) must beSome(dmilith)
            Users(db)(uuid3) must beNone
            Users(db).historyFor(uuid1) must haveSize(2)
            Users(db).historyFor(uuid1) must contain(teamon)
            Users(db).historyFor(uuid1) must contain(teamon2)
            Users(db).historyFor(uuid2) must beEmpty
            Users(db).historyFor(uuid3) must beEmpty
            
            db << dmilith2
            
            Users(db) must haveSize(2)
            Users(db)(uuid1) must beSome(teamon3)
            Users(db)(uuid2) must beSome(dmilith2)
            Users(db)(uuid3) must beNone
            Users(db).historyFor(uuid1) must haveSize(2)
            Users(db).historyFor(uuid1) must contain(teamon)
            Users(db).historyFor(uuid1) must contain(teamon2)
            Users(db).historyFor(uuid2) must haveSize(1)
            Users(db).historyFor(uuid2) must contain(dmilith)
            Users(db).historyFor(uuid3) must beEmpty
        }
        
        "not save identical object" in {
            val teamon = User("teamon")
            db << teamon
            db << teamon
            db << teamon
            db << teamon
            
            Users(db) must haveSize(1)
            Users(db).historyFor(teamon) must beEmpty
            
            val teamon2 = teamon.copy()
            db << teamon2
            
            Users(db) must haveSize(1)
            Users(db).historyFor(teamon) must beEmpty
        }

    }
    
}
