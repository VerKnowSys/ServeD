package com.verknowsys.served.db

import org.neodatis.odb._

class DBClient(val currentODB: ODB, val historyODB: ODB){
    /**
     * This two lines are required to make java.util.UUID work correctly with NeoDatis.
     * Because of implementation of `equals` method in java.util.UUID class which compares
     * `mostSigBits` and `leastSigBits` as well as `variant`. The `variant` field is declared
     * as `private transient int variant = -1`, so NeoDatis will not save it into database.
     * This field should have default assigned value `-1` but that happens only when object
     * is created using constructor, and NeoDatis uses reflection when retrieving objects
     * from database which leads to `variant` field with value of 0. Next, the `variant()`
     * method calculates UUID variant using `mostSigBits`, but ONLY if `variant` is -1.
     * Otherwise variant value will not be calculated. Since all UUIDs generated with java.util.UUID
     * class have variant 2, and UUID retrieved from NeoDatis database has variant 0 the same
     * object savend and then retrieved from NeoDatis is different in terms of `equals` method.
     * To solve this issue we need to force storing of transient `variant` field into database 
     * and that is exactly what those two lines do.
     *
     * @author teamon
     *
     * Reference: http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/6-b14/java/util/UUID.java
     */
    currentODB.getClassRepresentation(classOf[UUID].getName(), false).persistAttribute("variant")
    historyODB.getClassRepresentation(classOf[UUID].getName(), false).persistAttribute("variant")
    
    
    def <<[T <: DBObject : ClassManifest](newObj: T) = {
        // TODO: Hash neodatis internal oid when updating (relation consistency)
        // TODO: Update timestamp
        find(newObj.uuid) match {
            case Some(oldObj) if oldObj != newObj =>
                historyODB.store(oldObj)
                historyODB.commit
                currentODB.delete(oldObj)
                currentODB.store(newObj)
            case Some(oldObj) =>
            case None =>
                currentODB.store(newObj)
            
        }
        currentODB.commit
    }
    
    protected[db] def find[T <: DBObject : ClassManifest](uuid: UUID) = {
        val col = new TopLevelCollection[T](this)
        col(uuid)
    }
    
    def close {
        currentODB.close
        historyODB.close
    }
}

class DBServer(port: Int, path: String){
    OdbConfiguration.setLogServerStartupAndShutdown(false)
    
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
