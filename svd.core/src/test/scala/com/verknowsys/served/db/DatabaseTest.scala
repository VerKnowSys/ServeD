package com.verknowsys.served.db

import org.specs._
import com.verknowsys.served.SvdSpecHelpers._

abstract class DatabaseTest extends Specification {
    var server: DBServer = null
    var db: DBClient = null
    
    def reconnect {
        if(db != null) db.close
        if(server != null) server.close
        server = new DBServer(9000, "/tmp/svd_db_test/dbservertest")
        db = server.openClient
    }
    
    def connect {
        rmdir("/tmp/svd_db_test")
        mkdir("/tmp/svd_db_test")
        reconnect
    }
    
    def disconnect {
        if(db != null) db.close
        if(server != null) server.close
    }
}
