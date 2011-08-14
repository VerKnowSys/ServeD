package com.verknowsys.served.managers

import com.verknowsys.served.db.DBClient
import akka.actor.Actor

trait DatabaseAccess {
    self: Actor =>

    val db: DBClient

    override def postStop {
        db.close
    }
}
