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
            val i1 = new Item(1,1)
            val i2 = new Item(2,2)
            val i3 = new Item(3,3)
            
            val obj = EmbeddedList("first", i1 :: i2 :: i3 :: Nil)
            db << obj
            
            reconnect
            
            EmbeddedList(db)(obj.uuid) must beSome(obj)
        }
        
        "Use history for embedded objects" in {
            val i1 = new Item(1,1)
            val i2 = new Item(2,2)
            val i3 = new Item(3,3)
            val i4 = new Item(4,4)
            
            val obj = EmbeddedList("first", i1 :: i2 :: i3 :: Nil)
            val newobj = obj.copy(list = i4 :: obj.list)
            
            db << obj
            db << newobj
            
            reconnect
            
            EmbeddedList(db)(obj.uuid) must beSome(newobj)
            
            EmbeddedList(db).historyFor(obj.uuid) must haveSize(1)
            EmbeddedList(db).historyFor(obj.uuid) must contain(obj)
            
            // Console.readLine("press enter")
        }
    }
}
