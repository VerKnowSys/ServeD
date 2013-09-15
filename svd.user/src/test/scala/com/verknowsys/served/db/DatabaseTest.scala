/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.db

import com.verknowsys.served.testing._
// import org.specs._


abstract class DatabaseTest { //extends Specification {
    var server: DBServer = null
    var db: DBClient = null
    var path = randomPath

    def reconnect {
        if(db != null) db.close
        if(server != null) server.close
        server = new DBServer(randomPort, path)
        db = server.openClient
    }

    def connect {
        path = randomPath
        rmdir(path)
        mkdir(path)
        reconnect
    }

    def disconnect {
        if(db != null) db.close
        if(server != null) server.close
    }
}
