package com.verknowsys.served.db


import com.verknowsys.served.testing._
// import org.scalatest.FlatSpec
// import org.scalatest.matchers.ShouldMatchers


class PersistentObjectTest extends DatabaseTest with DefaultTest {

    override def beforeAll {
        connect
    }

    override def afterAll {
        disconnect
    }


    it should "set createdAt timestamp when created" in {
        val teamon  = User("teamon")
        teamon.createdAt must not be(null)
        val dmilith = User("dmilith")
        teamon.createdAt.compareTo(dmilith.createdAt) must be <=(0)
    }


    it should "be equal (without comparing createdAt)" in {
        val teamon1 = User("teamon")
        val teamon2 = teamon1.copy()
        teamon1 should equal(teamon2)
    }


    // it should "be not equal if uuid is different" in {
    //     val teamon1 = User("teamon")
    //     val teamon2 = User("teamon")
    //     teamon1 should not equal(teamon2)
    // }

}
