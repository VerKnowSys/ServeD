package com.verknowsys.served.db

import org.neodatis.odb._
import org.neodatis.odb.impl.core.query.criteria.CriteriaQuery
import org.neodatis.odb.core.query.criteria.Where
import org.neodatis.odb.core.query.nq.NativeQuery
import java.util.UUID
import java.io.Serializable
import scala.collection.JavaConversions._


class DBObject(val uuid: UUID) extends Serializable

class DBServer(port: Int, path: String){
    protected val server = ODBFactory.openServer(port)
    
    ("current" :: "history" :: Nil) foreach { suffix => 
        server.addBase(dbpath(suffix), dbpath(suffix) + ".neodatis")
    }
    
    server.startServer(true)
    
    def openClient = new DBClient(new DB(currentClient), new DB(historyClient))
    
    def currentClient = server.openClient(dbpath("current"))
    
    def historyClient = server.openClient(dbpath("history"))
    
    def close = server.close
    
    protected def dbpath(suffix: String) = path + "_" + suffix
}

class DB(val odb: ODB) {
    def <<(obj: DBObject) = odb.store(obj)
}

class DBManager[T <: DBObject : ClassManifest] {
    def apply(db: DB) = new {
        val klazz = classManifest[T].erasure
        
        def apply(predicate: T => Boolean): Iterable[T] = {
            asScalaIterable(db.odb.getObjects(new NativeQuery {
                setPolymorphic(true)

                def `match`(obj: Any): Boolean = obj match {
                    case x: T => predicate(x)
                    case _ => false
                }

                def getObjectType = klazz
            }))
        }
        
        def apply(uuid: UUID): Option[T] = apply(_.uuid.compareTo(uuid) == 0).headOption
        
        def all = asScalaIterable(db.odb.getObjects(new CriteriaQuery(klazz)))

        def count = db.odb.count(new CriteriaQuery(klazz))
    }
}

class DBClient(val current: DB, val history: DB)
