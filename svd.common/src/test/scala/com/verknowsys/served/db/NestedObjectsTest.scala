package com.verknowsys.served.db


import org.scalatest._
import org.scalatest.matchers._

import com.verknowsys.served._
import com.verknowsys.served.utils._
import com.verknowsys.served.api.Logger
import com.verknowsys.served.managers.LoggingManager
import com.verknowsys.served.testing._


class NestedObjectsTest extends DatabaseTest with DefaultTest {

    override def beforeAll {
        connect
    }

    override def afterAll {
        disconnect
    }


    it should "Persist object with empty list embedded" in {
        val obj = EmbeddedList("first", Nil)
        db << obj

        reconnect

        EmbeddedList(db)(obj.uuid) must be theSameInstanceAs(obj)
    }


    it should "Persist object with empty list embedded" in {
        val i1 = new Item(1,1)
        val i2 = new Item(2,2)
        val i3 = new Item(3,3)

        val obj = EmbeddedList("first", i1 :: i2 :: i3 :: Nil)
        db << obj

        reconnect

        EmbeddedList(db)(obj.uuid) must be theSameInstanceAs(obj)
    }


    it should "Use history for embedded objects" in {
        val i1 = new Item(1,1)
        val i2 = new Item(2,2)
        val i3 = new Item(3,3)
        val i4 = new Item(4,4)

        val obj = EmbeddedList("first", i1 :: i2 :: i3 :: Nil)
        val newobj = obj.copy(list = i4 :: obj.list)

        db << obj
        db << newobj

        reconnect

        EmbeddedList(db)(obj.uuid) must be theSameInstanceAs(newobj)

        EmbeddedList(db).historyFor(obj.uuid) must have size(1)
        EmbeddedList(db).historyFor(obj.uuid) must contain(obj)

        // Console.readLine("press enter")
    }

}
