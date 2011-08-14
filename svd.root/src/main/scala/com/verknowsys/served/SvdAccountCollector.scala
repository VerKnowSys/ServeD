package com.verknowsys.served


import com.verknowsys.served.db._
import com.verknowsys.served.utils._
import com.verknowsys.served.api.SvdAccount


/**
 *  @author dmilith
 *
 *   This util will read all /home folder contents and iterate through elements, then create user accounts database
 */

case object SvdAccounts extends DB[SvdAccount]


object SvdAccountCollector extends Logging {
    
    def main(args: Array[String]): Unit = {
        
        if (args.size == 0) {
            log.error("SvdAccountCollector requires path for source POSIX home dir.")
            sys.exit(1)
        }
        
        val argument = args.head
        val server = new DBServer(SvdConfig.remoteAccountServerPort, SvdConfig.systemHomeDir / SvdConfig.coreSvdAccountsDatabaseName)
        val db = server.openClient
        
        val homeDirectories = SvdUtils.listDirectories(argument)
        log.debug("Home directories: %s".format(homeDirectories.mkString(", ")))
        
        homeDirectories.foreach {
            dir =>
                val element = dir.getPath
                val userName = element.split("/").last
                val owner = SvdUtils.getOwner(element)
                if (owner == 0) {
                    log.error("Security violation! Cannot create user with UID == 0!")
                    sys.exit(1)
                }
                
                log.info("Processing account folder: %s. Owned by uid: %s".format(element, owner))
                val account = new SvdAccount(uid = owner, userName = userName)
                if (SvdAccounts(db)(_.uid == owner).isEmpty)
                    db << account
                else
                    log.warn("Account already imported: %s. Skipping.".format(account))
        }
        
        log.trace("Accounts in database after import: %s".format(
            for(account <- SvdAccounts(db))
                yield account
        ))
        
        db.close
        server.close
        log.info("Finished. All accounts imported")
    }
    
}