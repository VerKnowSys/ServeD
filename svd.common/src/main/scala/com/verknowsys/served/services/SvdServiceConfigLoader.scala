package com.verknowsys.served.services


import com.verknowsys.served._
import com.verknowsys.served.db._
import com.verknowsys.served.SvdConfig
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.Events._
import com.verknowsys.served.utils.signals.SvdPOSIX._
import com.verknowsys.served.services._
import com.verknowsys.served.api._
import com.verknowsys.served.api.accountkeys._
import com.verknowsys.served.api.pools._

import scala.io._


/**
 *
 * class to automatically load default configs of given software
 * it uses package object with implicit conversion from string to service configuration
 */
class SvdServiceConfigLoader(name: String) extends Logging {

    import net.liftweb.json._

    implicit val formats = DefaultFormats // Brings in default date formats etc.

    log.trace("SvdServiceConfigLoader: %s".format(name))

    val fullName = SvdConfig.defaultSoftwareTemplatesDir / name + SvdConfig.defaultSoftwareTemplateExt
    val defaultTemplate = parse(Source.fromFile(SvdConfig.defaultSoftwareTemplate + SvdConfig.defaultSoftwareTemplateExt).mkString) //.extract[SvdServiceConfig]
    val appSpecificTemplate = parse(Source.fromFile(fullName).mkString)
    val appTemplateMerged = defaultTemplate merge appSpecificTemplate

    // val svcName = (appSpecificTemplate \\ "name").extract[String]
    log.debug("Extracted SvdServiceConfig from igniter: %s.".format(name))
    // log.trace("Default template: %s".format(defaultTemplate))
    // log.trace("App template: %s".format(appSpecificTemplate))
    // log.trace("Merged template: %s".format(compact(render(appTemplateMerged))))

    val config = SvdServiceConfig( // OPTIMIZE: this should be done automatically
        name = (appTemplateMerged \ "name").extract[String],
        autoRestart = (appTemplateMerged \ "autoRestart").extract[Boolean],
        autoStart = (appTemplateMerged \ "autoStart").extract[Boolean],
        reportAllErrors = (appTemplateMerged \ "reportAllErrors").extract[Boolean],
        reportAllInfos = (appTemplateMerged \ "reportAllInfos").extract[Boolean],
        reportAllDebugs = (appTemplateMerged \ "reportAllDebugs").extract[Boolean],
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
