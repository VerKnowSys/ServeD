/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.db

import org.neodatis.odb._
import org.neodatis.odb.impl.core.query.criteria.CriteriaQuery
import org.neodatis.odb.core.query.nq.NativeQuery
import scala.collection.JavaConversions._

class TopLevelCollection[T <: Persistent : ClassManifest](db: DBClient) extends ClassQueryCollection[T](db.currentODB){
    def apply(uuid: UUID) = new FindByUUIDCollection(db.currentODB, uuid).headOption

    def historyFor(uuid: UUID): FindByUUIDCollection[T] = new FindByUUIDOrderedCollection(db.historyODB, uuid)

    def historyFor(obj: T): FindByUUIDCollection[T] = historyFor(obj.uuid)
}

class ClassQueryCollection[T <: Persistent : ClassManifest](odb: ODB) extends AbstractCollection[T] {
    def objects = odb.getObjects(objectType)

    def count = odb.count(new CriteriaQuery(objectType)).intValue

    def apply(f: T => Boolean) = new NativeQueryCollection(odb, f)
}

class FindByUUIDOrderedCollection[T <: Persistent : ClassManifest](odb: ODB, uuid: UUID) extends FindByUUIDCollection[T](odb, uuid) {
    override protected[db] def nativeQuery = {
        val nq = super.nativeQuery
        nq.orderByDesc("createdAt")
        nq
    }
}

class FindByUUIDCollection[T <: Persistent : ClassManifest](odb: ODB, uuid: UUID) extends AbstractCollection[T] {
    def objects = odb.getObjects(nativeQuery)

    protected[db] def nativeQuery = new NativeQuery {
        setPolymorphic(true)

        def `match`(obj: Any): Boolean = {
            val dbobj = obj.asInstanceOf[T]
            dbobj.uuid.getMostSignificantBits == uuid.getMostSignificantBits &&
                dbobj.uuid.getLeastSignificantBits == uuid.getLeastSignificantBits
        }

        def getObjectType = objectType
    }
}

class NativeQueryCollection[T <: Persistent : ClassManifest](odb: ODB, predicate: T => Boolean) extends AbstractCollection[T] {
    def objects = odb.getObjects(nativeQuery)

    protected[db] def nativeQuery = new NativeQuery {
        setPolymorphic(true)

        def `match`(obj: Any): Boolean = predicate(obj.asInstanceOf[T])

        def getObjectType = objectType
    }
}

abstract class AbstractCollection[T <: Persistent : ClassManifest] extends Iterable[T] {
    val objectType = classManifest[T].erasure

    def iterator = collectionAsScalaIterable(objects).iterator

    def objects: Objects[T]
}
