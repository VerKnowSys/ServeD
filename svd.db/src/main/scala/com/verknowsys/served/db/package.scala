package com.verknowsys.served

import org.neodatis.odb.Objects

package object db {
    implicit def dbclient2db(client: DBClient): DB = client.current
    // implicit def objects2iterable[T](objects: Objects[T]): DBObjects[T] = new DBObjects(objects)
}
