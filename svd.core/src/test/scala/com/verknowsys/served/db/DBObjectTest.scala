package com.verknowsys.served.db

import org.specs._
import com.verknowsys.served.SvdSpecHelpers._

class DBObjectTest extends Specification {
    var server: DBServer = null
    var db: DBClient = null

    "DBObject" should {
        doBefore {
            rmdir("/tmp/svd_db_test")
            mkdir("/tmp/svd_db_test")
            server = new DBServer(9000, "/tmp/svd_db_test/dbobjecttest")
            db = server.openClient
        }
        
        doAfter {
            server.close
        }
        
        "set createdAt timestamp when created" in {
            val teamon  = User("teamon")
            teamon.createdAt mustNot beNull
            
            val dmilith = User("dmilith")
            
            teamon.createdAt.compareTo(dmilith.createdAt) must be_<(0)
        }
        
        "be equal (without comparing createdAt)" in {
            val teamon1 = User("teamon")
            val teamon2 = teamon1.copy()
            
            teamon1 must_== teamon2
        }
        
        "be not equal if uuid is different" in {
            val teamon1 = User("teamon")
            val teamon2 = User("teamon")
            
            teamon1 mustNot beEqual(teamon2)
        }
        
    }
}
