package com.verknowsys.served.db

import org.specs._

class PersistentObjectTest extends DatabaseTest {
    "Persistent object" should {
        doBefore { connect }
        doAfter { disconnect }
        
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
