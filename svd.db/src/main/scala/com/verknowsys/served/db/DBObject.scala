package com.verknowsys.served.db

import org.neodatis.odb._

abstract class DBObject(val uuid: UUID = randomUUID){
    val createdAt = new java.util.Date
}
