package com.verknowsys.served.db

import com.mongodb.casbah.Imports._
import java.util.UUID

case class DBObj(uuid: UUID, data: (String, Any)*) {
    def toMongo = MongoDBObject(data:_*) + uuid
}

class DB {
    val mongoConn = MongoConnection()
    val mongoDB = mongoConn("served")

    val current = mongoDB("current")
    val history = mongoDB("history")

    setup

    def <<(dbobj: DBObj){
        val mobj = dbobj.toMongo
        current.findOne(dbobj.uuid) foreach { o =>
            // if(o != mobj){
                history.update(dbobj.uuid, $push("history" -> o), true, false)
                current -= o
            // }
        }
        current += mobj
    }

    def apply(uuid: UUID) = current.findOne(uuid)
    
    def historyFor(uuid: UUID): Iterable[DBObject] = 
        history.findOne(uuid).flatMap(_.getAs[BasicDBList]("history")).map(_.map(_.asInstanceOf[DBObject]).toList.reverse) getOrElse Nil
    
    
    def drop {
        current.drop
        history.drop
        setup
    }
    
    def setup {
        current.ensureIndex("uuid")
        history.ensureIndex("uuid")
    }
    
    def close = mongoConn.close
}
