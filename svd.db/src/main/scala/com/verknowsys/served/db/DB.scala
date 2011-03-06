package com.verknowsys.served.db

import com.mongodb.casbah.Imports._
import java.util.UUID

case class DBObj(uuid: UUID, pairs: (String, Any)*) {
    def toMongo = MongoDBObject(pairs:_*) + ("uuid" -> muuid)
    def muuid = (uuid.getMostSignificantBits, uuid.getLeastSignificantBits)
}

class DB {
    val mongoConn = MongoConnection()
    val mongoDB = mongoConn("served")
    
    val current = mongoDB("current")
    val history = mongoDB("history")
    
    setup

    
    def <<(dbobj: DBObj){
        val mobj = dbobj.toMongo
        current.findOne(MongoDBObject("uuid" -> dbobj.muuid)) foreach { o =>
            // if(o != mobj){
                history.update(MongoDBObject("uuid" -> dbobj.muuid), $push("history" -> o), true, false)
                current -= o
            // }
        }
        current += mobj
    }

    def apply(uuid: UUID) = current.findOne(MongoDBObject("uuid" -> (uuid.getMostSignificantBits, uuid.getLeastSignificantBits)))

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
