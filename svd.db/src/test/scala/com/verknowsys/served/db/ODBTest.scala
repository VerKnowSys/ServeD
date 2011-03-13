package com.verknowsys.served.db

import java.util.UUID
import com.mongodb.casbah.Imports._
import org.specs._

case class User(uuid: UUID, name: String) extends DBO(uuid)
case class Drug(uuid: UUID, name: String) extends DBO(uuid)

class ODBTest extends Specification {
    var db: DB = null
    
    "ODB" should {
        
        doBefore {
            db = new DB
            db.drop
        }
        
        doAfter {
            db.close
        }
        
        "store Users" in {
            val teamon  = User(UUID.randomUUID, "teamon")
            val dmilith = User(UUID.randomUUID, "dmilith")
            
            db << teamon
            db << dmilith
            
            db.current.size must_== 2
            
            val users = db.all[User].toList
            users must haveSize(2)
            users must contain(teamon)
            users must contain(dmilith)
        }
        
        "store Users and Drug" in {
            val teamon  = User(UUID.randomUUID, "teamon")
            val dmilith = User(UUID.randomUUID, "dmilith")
            val cig     = Drug(UUID.randomUUID, "cig")
            val joint   = Drug(UUID.randomUUID, "joint")
            
            db << teamon
            db << dmilith
            db << cig
            db << joint
            
            db.current.size must_== 4
            
            val users = db.all[User].toList
            users must haveSize(2)
            users must contain(teamon)
            users must contain(dmilith)
            
            val drugs = db.all[Drug].toList
            drugs must haveSize(2)
            drugs must contain(cig)
            drugs must contain(joint)
        }
        
        "query by map" in {
            val teamon  = User(UUID.randomUUID, "teamon")
            val dmilith = User(UUID.randomUUID, "dmilith")
            val lopex   = User(UUID.randomUUID, "lopex") // He helps a lot //teamon
            
            db << teamon
            db << dmilith
            db << lopex
            
            val result1 = db.all[User]("name" -> "teamon").toList
            result1 must haveSize(1)
            result1 must contain(teamon)
            
            val result2 = db.all[User]("name" -> "lopex").toList
            result2 must haveSize(1)
            result2 must contain(lopex)
        }
        
        // "query by uuid" in {
        //     val uuid1 = UUID.randomUUID
        //     val uuid2 = UUID.randomUUID
        //     val uuid3 = UUID.randomUUID
        //     
        //     val teamon  = User(uuid1, "teamon")
        //     val dmilith = User(uuid2, "dmilith")
        //     
        //     db << teamon
        //     db << dmilith
        //     
        //     db[User](uuid1) must beSome(teamon)
        //     db[User](uuid2) must beSome(dmilith)
        //     db[User](uuid3) must beNone
        // }
    }
}
