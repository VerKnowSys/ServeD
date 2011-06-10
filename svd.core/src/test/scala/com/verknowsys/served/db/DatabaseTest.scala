package com.verknowsys.served.db

import org.specs._
import com.verknowsys.served.SvdSpecHelpers._
import com.verknowsys.served.utils._
import com.verknowsys.served._


abstract class DatabaseTest extends Specification {
    var server: DBServer = null
    var db: DBClient = null
    
    def reconnect {
        if(db != null) db.close
        if(server != null) server.close
        server = new DBServer(9000, SvdConfig.systemTmpDir / "svd_db_test/dbservertest")
        db = server.openClient
    }
    
    def connect {
        rmdir(SvdConfig.systemTmpDir / "svd_db_test")
        mkdir(SvdConfig.systemTmpDir / "svd_db_test")
        reconnect
    }
    
    def disconnect {
        if(db != null) db.close
        if(server != null) server.close
    }
}
