/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

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
