/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.services


import com.verknowsys.served.SvdConfig
import com.verknowsys.served.utils._
import com.verknowsys.served.api._

import java.io.FileNotFoundException
import scala.io._
import org.json4s._
import org.json4s.native.JsonMethods._


/**
 *
 * class to automatically load default configs of given software
 * it uses package object with implicit conversion from string to service configuration
 */
class SvdServiceConfigLoader(name: String) extends Logging with SvdUtils {

    implicit val formats = DefaultFormats // Brings in default date formats etc.

    log.trace("SvdServiceConfigLoader: %s".format(name))

    val fullName = SvdConfig.defaultSoftwareTemplatesDir / name + SvdConfig.defaultSoftwareTemplateExt
    val defaultTemplate = parse(Source.fromFile(SvdConfig.defaultSoftwareTemplate + SvdConfig.defaultSoftwareTemplateExt).mkString) //.extract[SvdServiceConfig]
    val appSpecificTemplate = parse(
        try {
            Source.fromFile(fullName).mkString
        } catch {
            case x: FileNotFoundException => // template not exists in common igniter location, try at user space
                val userSideIgniter = SvdConfig.userHomeDir / "%d".format(getUserUid) / SvdConfig.defaultUserIgnitersDir / name + SvdConfig.defaultSoftwareTemplateExt
                log.debug("Common igniter not found: %s. Trying again with: %s", fullName, userSideIgniter)
                Source.fromFile(userSideIgniter).mkString

            // case x: Exception => // template not exists
                // log.error("SvdServiceConfigLoader Failure with igniter: %s", name)
        }
    )
    val appTemplateMerged = defaultTemplate merge appSpecificTemplate

    // val svcName = (appSpecificTemplate \\ "name").extract[String]
    log.debug("Extracted SvdServiceConfig from igniter: %s.".format(name))
    // log.trace("Default template: %s".format(defaultTemplate))
    // log.trace("App template: %s".format(appSpecificTemplate))
    log.trace(s"Merged template: ${compact(render(appTemplateMerged))}")


    def config = SvdServiceConfig( // OPTIMIZE: this should be done automatically
        name = name,
        softwareName = (appTemplateMerged \ "softwareName").extract[String],
        autoRestart = (appTemplateMerged \ "autoRestart").extract[Boolean],
        autoStart = (appTemplateMerged \ "autoStart").extract[Boolean],
        reportAllErrors = (appTemplateMerged \ "reportAllErrors").extract[Boolean],
        reportAllInfos = (appTemplateMerged \ "reportAllInfos").extract[Boolean],
        reportAllDebugs = (appTemplateMerged \ "reportAllDebugs").extract[Boolean],
        schedulerActions = (appTemplateMerged \ "schedulerActions").children.map {
            children =>
                SvdSchedulerActions(
                    cronEntry = (children \ "cronEntry").extract[String],
                    shellCommands = (children \ "shellCommands").extract[List[String]],
                    jvmCommands = (children \ "jvmCommands").extract[List[String]]
                )
            },

        install = SvdShellOperations(
                commands = (appTemplateMerged \ "install" \ "commands").extract[List[String]],
                expectStdOut = (appTemplateMerged \ "install" \ "expectStdOut").extract[List[String]],
                expectStdErr = (appTemplateMerged \ "install" \ "expectStdErr").extract[List[String]],
                expectOutputTimeout = (appTemplateMerged \ "install" \ "expectOutputTimeout").extract[Int]
            ),
        configure = SvdShellOperations(
                commands = (appTemplateMerged \ "configure" \ "commands").extract[List[String]],
                expectStdOut = (appTemplateMerged \ "configure" \ "expectStdOut").extract[List[String]],
                expectStdErr = (appTemplateMerged \ "configure" \ "expectStdErr").extract[List[String]],
                expectOutputTimeout = (appTemplateMerged \ "configure" \ "expectOutputTimeout").extract[Int]
            ),
        start = SvdShellOperations(
                commands = (appTemplateMerged \ "start" \ "commands").extract[List[String]],
                expectStdOut = (appTemplateMerged \ "start" \ "expectStdOut").extract[List[String]],
                expectStdErr = (appTemplateMerged \ "start" \ "expectStdErr").extract[List[String]],
                expectOutputTimeout = (appTemplateMerged \ "start" \ "expectOutputTimeout").extract[Int]
            ),
        afterStart = SvdShellOperations(
                commands = (appTemplateMerged \ "afterStart" \ "commands").extract[List[String]],
                expectStdOut = (appTemplateMerged \ "afterStart" \ "expectStdOut").extract[List[String]],
                expectStdErr = (appTemplateMerged \ "afterStart" \ "expectStdErr").extract[List[String]],
                expectOutputTimeout = (appTemplateMerged \ "afterStart" \ "expectOutputTimeout").extract[Int]
            ),
        stop = SvdShellOperations(
                commands = (appTemplateMerged \ "stop" \ "commands").extract[List[String]],
                expectStdOut = (appTemplateMerged \ "stop" \ "expectStdOut").extract[List[String]],
                expectStdErr = (appTemplateMerged \ "stop" \ "expectStdErr").extract[List[String]],
                expectOutputTimeout = (appTemplateMerged \ "stop" \ "expectOutputTimeout").extract[Int]
            ),
        afterStop = SvdShellOperations(
                commands = (appTemplateMerged \ "afterStop" \ "commands").extract[List[String]],
                expectStdOut = (appTemplateMerged \ "afterStop" \ "expectStdOut").extract[List[String]],
                expectStdErr = (appTemplateMerged \ "afterStop" \ "expectStdErr").extract[List[String]],
                expectOutputTimeout = (appTemplateMerged \ "afterStop" \ "expectOutputTimeout").extract[Int]
            ),
        reload = SvdShellOperations(
                commands = (appTemplateMerged \ "reload" \ "commands").extract[List[String]],
                expectStdOut = (appTemplateMerged \ "reload" \ "expectStdOut").extract[List[String]],
                expectStdErr = (appTemplateMerged \ "reload" \ "expectStdErr").extract[List[String]],
                expectOutputTimeout = (appTemplateMerged \ "reload" \ "expectOutputTimeout").extract[Int]
            ),
        validate = SvdShellOperations(
                commands = (appTemplateMerged \ "validate" \ "commands").extract[List[String]],
                expectStdOut = (appTemplateMerged \ "validate" \ "expectStdOut").extract[List[String]],
                expectStdErr = (appTemplateMerged \ "validate" \ "expectStdErr").extract[List[String]],
                expectOutputTimeout = (appTemplateMerged \ "validate" \ "expectOutputTimeout").extract[Int]
            )
        )

}
