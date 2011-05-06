package com.verknowsys.served.db

import org.neodatis.odb._
import org.neodatis.odb.impl.core.query.criteria.CriteriaQuery
import org.neodatis.odb.core.query.nq.NativeQuery
import scala.collection.JavaConversions._

class TopLevelCollection[T <: DBObject : ClassManifest](db: DBClient) extends ClassQueryCollection[T](db.currentODB){
    def apply(uuid: UUID) = new NativeQueryCollection(db.currentODB, findByUUID(uuid)).headOption
    
    def historyFor(uuid: UUID) = new NativeQueryCollection(db.historyODB, findByUUID(uuid))
    
    protected[db] def findByUUID(uuid: UUID): T => Boolean = _.uuid.compareTo(uuid) == 0
}

class ClassQueryCollection[T <: DBObject : ClassManifest](odb: ODB) extends AbstractCollection[T] {
    def objects = odb.getObjects(objectType)
    
    def count = odb.count(new CriteriaQuery(objectType)).intValue
    
    def apply(f: T => Boolean) = new NativeQueryCollection(odb, f)
}

class NativeQueryCollection[T <: DBObject : ClassManifest](odb: ODB, predicate: T => Boolean) extends AbstractCollection[T] {
    def objects = odb.getObjects(nativeQuery)
    
    protected[db] def nativeQuery = new NativeQuery {
        setPolymorphic(true)

        def `match`(obj: Any): Boolean = predicate(obj.asInstanceOf[T])

        def getObjectType = objectType
    }
}

abstract class AbstractCollection[T <: DBObject : ClassManifest] extends Iterable[T] {
    val objectType = classManifest[T].erasure

    def iterator = asScalaIterable(objects).iterator

    def objects: Objects[T]
}
