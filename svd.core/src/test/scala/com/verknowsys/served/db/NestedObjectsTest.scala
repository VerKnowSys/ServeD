package com.verknowsys.served.db

import org.specs._

class NestedObjectsTest extends DatabaseTest {
    "NestedObjects" should {
        doBefore { connect }
        doAfter { disconnect }
        
        "Persist object with empty list embedded" in {
            val obj = EmbeddedList("first", Nil)
            db << obj
            
            reconnect
            
            EmbeddedList(db)(obj.uuid) must beSome(obj)
        }
        
        "Persist object with empty list embedded" in {
            val obj = EmbeddedList("first", List(1,2,3))
            db << obj
            
            reconnect
            
            EmbeddedList(db)(obj.uuid) must beSome(obj)
        }
    }
}
