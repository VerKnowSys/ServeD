package com.verknowsys.served.web


import com.verknowsys.served.utils._


object DatabaseServer {

    import com.verknowsys.served.db._
    import com.verknowsys.served.web.merch._

    val dbPostfix = "db/database"
    val dbPort = 34569
    val server = new DBServer(dbPort, dbPostfix)

}


trait Database extends Logging with SvdUtils {

    import webImplicits._
    import com.verknowsys.served.db._


    var db: DBClient = null


    def message(message: String) =
        """{"message": "%s"}""" format Dict(message)


    def initDB = {
        DatabaseServer

        if (db == null) {
            log.info("Database client is now starting")
            db = DatabaseServer.server.openClient
        } else {
            log.info("Database client started")
        }
    }


    def terminate = {
        db.close
        log.info("Database shutdown requested")
    }


    initDB // initialize database client connection
}