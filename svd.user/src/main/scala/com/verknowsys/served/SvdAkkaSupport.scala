package com.verknowsys.served


import com.verknowsys.served._
import com.verknowsys.served.db._
import com.verknowsys.served.utils._

import scala.io.Source
import java.io._
import scala.util._


trait SvdAkkaSupport extends SvdUtils with Logging {

    /**
     *  @author dmilith
     *
     *   Creates akka configuration file for given user.
     *   It might be used to create akka configuration for headless svduser
     */
    def createAkkaUserConfIfNotExistant(uid: Int, userManagerPort: Int) = {
        val configFile = SvdConfig.userHomeDir / "%d".format(uid) / SvdConfig.defaultAkkaConfName

        if (!new File(configFile).exists) {
            log.info("Akka config: %s not found. Generating default one", configFile)

            val defaultConfig = Source.fromURL(
                getClass.getResource(
                    SvdConfig.defaultUserAkkaConf
                )
            ).getLines.mkString("\n").replaceAll("USER_NETTY_PORT", "%d".format(userManagerPort))
            writeToFile(configFile, defaultConfig)
        } else {
            log.info("Akka config found: %s", configFile)
        }
    }

}