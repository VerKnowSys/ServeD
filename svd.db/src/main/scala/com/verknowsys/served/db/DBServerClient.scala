package com.verknowsys.served.db

import org.neodatis.odb._

abstract class DBObject(val uuid: UUID = randomUUID) // TODO: Add timestamp

class DBClient(val currentODB: ODB, val historyODB: ODB){
    def <<[T <: DBObject : ClassManifest](newObj: T) = {
        // TODO: Hash neodatis internal oid when updating (relation consistency)
        // TODO: Update timestamp
        find(newObj.uuid) match {
            case Some(oldObj) =>
                historyODB.store(oldObj)
                historyODB.commit
                currentODB.delete(oldObj)
                currentODB.store(newObj)
            case None =>
                currentODB.store(newObj)
        }
        currentODB.commit
    }
    
    protected[db] def find[T <: DBObject : ClassManifest](uuid: UUID) = {
        val col = new TopLevelCollection[T](this)
        col(uuid)
    }
}

class DBServer(port: Int, path: String){
    protected val server = ODBFactory.openServer(port)
    
    ("current" :: "history" :: Nil) foreach { suffix => 
        server.addBase(dbpath(suffix), dbpath(suffix) + ".neodatis")
    }
    
    server.startServer(true)
    
    def openClient = new DBClient(currentClient, historyClient)
    
    def currentClient = server.openClient(dbpath("current"))
    
    def historyClient = server.openClient(dbpath("history"))
    
    def close = server.close
    
    protected def dbpath(suffix: String) = path + "_" + suffix
}

class DB[T <: DBObject : ClassManifest] {
    def apply(db: DBClient) = new TopLevelCollection[T](db)
}
