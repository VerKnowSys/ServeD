package com.verknowsys.served.db

import com.mongodb.casbah.Imports._
import scala.collection.JavaConversions._
import java.util.UUID

import com.novus.salat._
import com.novus.salat.global._

case class DBObj(uuid: UUID, data: (String, Any)*) {
    def toMongo = MongoDBObject(data:_*) + uuid
}

class DB {
    val mongoConn = MongoConnection()
    val mongoDB = mongoConn("served")

    val current = mongoDB("current")
    val history = mongoDB("history")

    setup

    def apply(uuid: UUID) = current.findOne(uuid)

    def update(uuid: UUID, obj: MongoDBObject): Unit = {
        current.findOne(uuid) match {
            case Some(oldObj) =>
                val oldMap = oldObj.toMap
                oldMap.remove("_id")
                oldMap.remove("uuid")
                if(oldMap != obj.toMap){
                    history.update(uuid, $push("history" -> oldObj), true, false)
                    current -= oldObj
                    current += (obj + uuid)
                }
            case None =>
                current += (obj + uuid)
        }
    }

    def update(uuid: UUID, data: (String, Any)*): Unit = update(uuid, MongoDBObject(data:_*))

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
    
    def all[T <: CaseClass](implicit manifest: Manifest[T]) = current.find(MongoDBObject("_typeHint" -> manifest.toString)).map(grater[T].asObject(_))
    
    def all[T <: CaseClass : Manifest](pairs: (String, Any)*) = current.find(Map("_typeHint" -> manifest[T].toString) ++ pairs).map(grater[T].asObject(_))
    
    def <<[T <: CaseClass : Manifest](obj: T) = current.insert(grater[T].asDBObject(obj))
}
