package com.verknowsys.served.db

import org.neodatis.odb._
import org.neodatis.odb.core.NeoDatisError
import com.verknowsys.served.utils.Logging

class DBClient(val currentODB: ODB, val historyODB: ODB) extends Logging {
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
    
    /**
     * Save object in database
     * 
     * If object with the same uuid already exists in database it will be moved to history
     * database and replaced in current database with current one.
     *
     * @author teamon
     */
    def <<[T <: Persistent : ClassManifest](newObj: T) = {
        // TODO: Hash neodatis internal oid when updating (relation consistency)
        find(newObj.uuid) match {
            case Some(oldObj) if oldObj != newObj =>
                historyODB.store(oldObj)
                historyODB.commit
                currentODB.store(newObj)
                currentODB.delete(oldObj)
                currentODB.commit
            
            case Some(oldObj) =>
                // TODO: Update timestamp
            
            case None =>
                currentODB.store(newObj)
                currentODB.commit
                historyODB.store(newObj)
                historyODB.commit
        }
    }
    
    /**
     * Remove object from database
     *
     * @author teamon
     */
    def ~[T <: Persistent : ClassManifest](obj: T) { this ~ obj.uuid }
    
    /**
     * Remove object with given uuid from database
     *
     * @author teamon
     */
    def ~[T <: Persistent : ClassManifest](uuid: UUID) = find(uuid) foreach { obj =>
        (new FindByUUIDOrderedCollection[T](historyODB, obj.uuid)).headOption match {
            case Some(o) if o == obj =>
            case _ =>
                historyODB.store(obj)
                historyODB.commit
        }

        currentODB.delete(obj)
        currentODB.commit
    }
    
    protected[db] def find[T <: Persistent : ClassManifest](uuid: UUID) = {
        val col = new TopLevelCollection[T](this)
        col(uuid)
    }
    
    /**
     * Close database connection
     *
     * @author teamon
     */
    def close {
        try {
            if(!currentODB.isClosed()) currentODB.close
            if(!historyODB.isClosed()) historyODB.close
        } catch {
            case ex: ODBRuntimeException =>
                log.warn("ODBRuntimeException: %s", ex.toString)// XXX: Should we do something with it?
            case ex: NeoDatisError => 
                log.warn("NeoDatisError: %s", ex.toString)// XXX: Should we do something with it?
        }
    }
}

/**
 * Server instance.
 * 
 * It requires port and path to database directory.
 * When given path = "/db/mybase" it will create two databases:
 *  - "/db/mybase_current.neodatis" for the most up-to-date objects
 *  - "/db/mybase_history.neodatis" for history of changes
 *
 * @author teamon
 */
class DBServer(port: Int, path: String){
    OdbConfiguration.setLogServerStartupAndShutdown(false)
    OdbConfiguration.setDatabaseCharacterEncoding("UTF-8");
    
    protected val server = ODBFactory.openServer(port)
    
    ("current" :: "history" :: Nil) foreach { suffix => 
        server.addBase(dbpath(suffix), dbpath(suffix) + ".neodatis")
    }
    
    server.startServer(true)
    
    /**
     * Returns new client instance for this server
     *
     * @author teamon
     */
    def openClient = new DBClient(currentClient, historyClient)
    
    /**
     * Shutdown server
     *
     * @author teamon
     */
    
    def close = server.close
    
    protected def currentClient = server.openClient(dbpath("current"))
    
    protected def historyClient = server.openClient(dbpath("history"))
    
    protected def dbpath(suffix: String) = path + "_" + suffix
}

/**
 * Factory object for type-safe manipulation on database
 *
 * @example
 *    object Foo extends DB[Foo]
 *    Foo(db) // provides access to query operations on `Foo` objects
 *
 * @author teamon
 */
class DB[T <: Persistent : ClassManifest] {
    def apply(db: DBClient) = new TopLevelCollection[T](db)
}
