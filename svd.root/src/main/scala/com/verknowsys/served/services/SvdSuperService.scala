/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.services


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
import org.quartz.{TriggerBuilder, JobBuilder, CronScheduleBuilder}


/**
 *  SuperService is a SvdService clone, but to run as superuser only.
 *
 * @author Daniel (dmilith) Dettlaff
 */
class SvdSuperService(config: SvdServiceConfig) extends SvdService(config, new SvdAccount(uid = 0, userName = "SuperUser")) with SvdActor with SvdUtils {


    val prefixDir = SvdConfig.softwareRoot / config.softwareName
    val dataDir = SvdConfig.systemHomeDir / SvdConfig.softwareDataDir / config.softwareName


    override def preStart = {
        log.trace(s"Checking dirs: ${prefixDir}, ${dataDir}")
        checkOrCreateDir(prefixDir)
        checkOrCreateDir(dataDir)
        log.debug(s"Prestarting ${this}")
        super.preStart
    }


    override def serviceRootPrefix = prefixDir


    override def servicePrefix = dataDir


    override def installIndicator = new File(
        prefixDir / config.softwareName.toLowerCase + "." + SvdConfig.installed)


    override def toString = "SvdSuperService name: %s. Uptime: %s".format(config.name, secondsToHMS((JSystem.currentTimeMillis - uptime).toInt / 1000))


}

