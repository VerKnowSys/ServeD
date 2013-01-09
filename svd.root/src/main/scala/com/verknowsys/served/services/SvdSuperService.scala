/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.services


import com.verknowsys.served._
import com.verknowsys.served.services._
import com.verknowsys.served.api._
import com.verknowsys.served.api.scheduler._
//import com.verknowsys.served.db._
import com.verknowsys.served.SvdConfig
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.utils._
import com.verknowsys.served.scheduler._

import scala.io._
import java.io.File
import java.text._
import akka.actor._
import scala.concurrent._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import java.lang.{System => JSystem}
// import org.quartz.{TriggerBuilder, JobBuilder, CronScheduleBuilder}


/**
 *  SuperService is a SvdService clone, but to run as superuser only.
 *
 * @author Daniel (dmilith) Dettlaff
 */
class SvdSuperService(config: SvdServiceConfig) extends SvdService(config = config, account = new SvdAccount(uid = 0, userName = "SuperUser")) with Logging {


    override val serviceRootPrefix = SvdConfig.softwareRoot / config.softwareName
    override val servicePrefix = SvdConfig.systemHomeDir / SvdConfig.softwareDataDir / config.name


    override def preStart = {
        log.trace(s"Checking dirs: ${serviceRootPrefix}, ${servicePrefix}")
        log.debug(s"Prestarting ${this}")
        checkOrCreateDir(serviceRootPrefix)
        checkOrCreateDir(servicePrefix)

        super.preStart
    }


    override def installIndicator = new File(
        serviceRootPrefix / config.softwareName.toLowerCase + "." + SvdConfig.installed)


    override def toString = "SvdSuperService name: %s. Uptime: %s".format(config.name, secondsToHMS((JSystem.currentTimeMillis - uptime).toInt / 1000))


}

