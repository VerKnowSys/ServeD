package com.verknowsys.served.db

import org.specs._
import com.verknowsys.served.SvdSpecHelpers._
import com.verknowsys.served.utils._
import com.verknowsys.served._


abstract class DatabaseTest extends Specification {
    var server: DBServer = null
    var db: DBClient = null
    var path = randomPath
    
    def reconnect {
        if(db != null) db.close
        if(server != null) server.close
        server = new DBServer(9000, path)
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
