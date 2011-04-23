package com.verknowsys.served.db

import org.neodatis.odb._
import org.neodatis.odb.impl.core.query.criteria.CriteriaQuery
import java.util.UUID

class DBObject(uuid: UUID)

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
    
    def count[T <: DBObject : ClassManifest] =
        odb.count(new CriteriaQuery(classManifest[T].erasure))
}

class DBClient(val current: DB, val history: DB){
    
}
