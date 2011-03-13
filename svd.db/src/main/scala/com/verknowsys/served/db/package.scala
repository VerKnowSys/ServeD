package com.verknowsys.served

import com.mongodb.casbah.Imports._
import java.util.UUID

package object db {
    // implicit def uuid2MongoDBObject(uuid: UUID) = MongoDBObject("uuid" -> (uuid.getMostSignificantBits, uuid.getLeastSignificantBits))
    // implicit def uuid2Pair(uuid: UUID) = ("uuid" -> (uuid.getMostSignificantBits, uuid.getLeastSignificantBits))
}
