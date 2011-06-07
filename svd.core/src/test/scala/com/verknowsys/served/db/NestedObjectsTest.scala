package com.verknowsys.served.db

import org.specs._

class NestedObjectsTest extends DatabaseTest {
    "NestedObjects" should {
        doBefore { connect }
        doAfter { disconnect }
        
        "Persist with empty list" in {
            val first = EmbeddedList("first", Nil)
            db << first
            
            reconnect
            
            EmbeddedList(db)(first.uuid) must beSome(first)
        }
    }
}
