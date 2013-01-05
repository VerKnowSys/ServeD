/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.db


import com.verknowsys.served.testing._


class DBTest extends DatabaseTest with DefaultTest {

    override def beforeAll {
        reconnect
    }

    override def afterAll {
        disconnect
    }


    it should "Data model" in {
        val teamon = new User("teamon")
        val teamon2 = teamon.copy()
        val res1 = "%s".format(teamon.uuid)
        val res2 = "%s".format(teamon2.uuid)
        res1 must be(res2)
    }


    it should "store Users" in {
        val teamon = new User("teamon")
        val dmilith = new User("dmilith")

        db << teamon
        db << dmilith

        reconnect

        Users(db).count must be(2)


        val users1 = Users(db)
        users1 must have size(2)
        users1 must contain(teamon)
        users1 must contain(dmilith)
    }


    it should "delete user by object" in {
        val teamon = new User("teamon")
        val dmilith = new User("dmilith")

        db << teamon
        db << dmilith

        reconnect

        db ~ teamon

        Users(db).count must be(1)
        val users1 = Users(db)
        users1 must have size(1)
        users1 must not contain(teamon)
        users1 must contain(dmilith)

        users1.historyFor(teamon.uuid) must have size(1)
        users1.historyFor(teamon.uuid) must contain(teamon)
        users1.historyFor(dmilith.uuid) must contain(dmilith)


        db << teamon
        db ~ teamon

        users1.historyFor(teamon.uuid) must have size(2)
        users1.historyFor(teamon.uuid) must contain(teamon)

        db << teamon.copy(name = "teamon 1")
        db ~ teamon
    }


    it should "store Users and Drugs" in {
        val teamon  = new User("teamon")
        val dmilith = new User("dmilith")
        val cig     = new Drug("cig")
        val joint   = new Drug("joint")

        db << teamon
        db << dmilith
        db << cig
        db << joint

        reconnect

        val users1 = Users(db)
        users1 must have size(2)
        users1 must contain(teamon)
        users1 must contain(dmilith)

        val drugs1 = Drugs(db)
        drugs1 must have size(2)
        drugs1 must contain(cig)
        drugs1 must contain(joint)
    }


    it should "query by predicate" in {
        val teamon = new User("teamon")
        val dmilith = new User("dmilith")
        val lopex = new User("lopex")

        db << teamon
        db << dmilith
        db << lopex
        db << Drug("a")
        db << Drug("b")
        db << Drug("c")

        reconnect

        val users1 = Users(db)(e => true)
        users1 must have size(3)
        users1 must contain(teamon)
        users1 must contain(dmilith)
        users1 must contain(lopex)

        val users2 = Users(db)(_.name == "teamon")
        users2 must have size(1)
        users2 must contain(teamon)

        val users3 = Users(db){ e => e.name == "teamon" || e.name == "dmilith" }
        users3 must have size(2)
        users3 must contain(teamon)
        users3 must contain(dmilith)

        val users4 = Users(db)(_.name == "lopex")
        users4 must have size(1)
        users4 must contain(lopex)
    }


    it should "query by uuid" in {
        val teamon  = User("teamon")
        val dmilith = User("dmilith")

        val uuid1 = teamon.uuid
        val uuid2 = dmilith.uuid
        val uuid3 = randomUUID

        db << teamon
        db << dmilith

        reconnect

        Users(db)(uuid1) must be (Some(teamon))
        Users(db)(uuid2) must be (Some(dmilith))
        Users(db)(uuid3) must be(None)
    }


    it should "use history" in {
        val teamon  = User("teamon")
        val dmilith = User("dmilith")

        val uuid1 = teamon.uuid
        val uuid2 = dmilith.uuid
        val uuid3 = randomUUID

        db << teamon
        db << dmilith

        reconnect

        Users(db) must have size(2)
        Users(db).historyFor(uuid1) must have size(1)
        Users(db).historyFor(uuid1) must contain(teamon)
        Users(db).historyFor(uuid2) must have size(1)
        Users(db).historyFor(uuid2) must contain(dmilith)
        Users(db).historyFor(uuid3) must be('empty)

        val teamon2 = teamon.copy(name = "teamon2")
        val teamon3 = teamon.copy(name = "teamon3")
        val dmilith2 = dmilith.copy(name = "dmilith2")

        db << teamon2

        reconnect

        Users(db) must have size(2)
        Users(db)(uuid1) must be (Some(teamon2))
        Users(db)(uuid2) must be (Some(dmilith))
        Users(db)(uuid3) must be(None)
        Users(db).historyFor(uuid1) must have size(1)
        Users(db).historyFor(uuid1) must contain(teamon)
        Users(db).historyFor(uuid2) must have size(1)
        Users(db).historyFor(uuid2) must contain(dmilith)
        Users(db).historyFor(uuid3) must be('empty)

        db << teamon3

        reconnect

        Users(db) must have size(2)
        Users(db)(uuid1) must be (Some(teamon3))
        Users(db)(uuid2) must be (Some(dmilith))
        Users(db)(uuid3) must be(None)
        Users(db).historyFor(uuid1) must have size(2)
        Users(db).historyFor(uuid1) must contain(teamon)
        Users(db).historyFor(uuid1) must contain(teamon2)
        Users(db).historyFor(uuid2) must have size(1)
        Users(db).historyFor(uuid2) must contain(dmilith)
        Users(db).historyFor(uuid3) must be('empty)

        db << dmilith2

        reconnect

        Users(db) must have size(2)
        Users(db)(uuid1) must be (Some(teamon3))
        Users(db)(uuid2) must be (Some(dmilith2))
        Users(db)(uuid3) must be(None)
        Users(db).historyFor(uuid1) must have size(2)
        Users(db).historyFor(uuid1) must contain(teamon)
        Users(db).historyFor(uuid1) must contain(teamon2)
        Users(db).historyFor(uuid2) must have size(1)
        Users(db).historyFor(uuid2) must contain(dmilith)
        Users(db).historyFor(uuid3) must be('empty)
    }


    it should "not save identical object" in {
        val teamon = User("teamon")
        db << teamon
        db << teamon
        db << teamon
        db << teamon

        reconnect

        Users(db) must have size(1)
        Users(db).historyFor(teamon) must have size(1)
        Users(db).historyFor(teamon) must contain(teamon)

        val teamon2 = teamon.copy()
        db << teamon2

        reconnect

        Users(db) must have size(1)
        Users(db).historyFor(teamon) must have size(1)
        Users(db).historyFor(teamon) must contain(teamon)
    }


    it should "sort history by descending createdAt" in {
        val teamon1 = User("teamon 1")
        db << teamon1
        val teamon2 = teamon1.copy(name = "teamon 2")
        db << teamon2
        val teamon3 = teamon1.copy(name = "teamon 3")
        db << teamon3
        val teamon4 = teamon1.copy(name = "teamon 4")
        db << teamon4
        val teamon5 = teamon1.copy(name = "teamon 5")
        db << teamon5

        reconnect

        Users(db).historyFor(teamon1) must have size(4)
        Users(db).historyFor(teamon1).toList must be(teamon4 :: teamon3 :: teamon2 :: teamon1 :: Nil)
    }

}
