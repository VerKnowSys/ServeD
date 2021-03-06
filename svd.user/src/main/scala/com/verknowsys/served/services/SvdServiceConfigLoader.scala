// /*
//  * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
//  * This Software is a close code project. You may not redistribute this code without permission of author.
//  */

// package com.verknowsys.served.services


// import com.verknowsys.served.SvdConfig
// import com.verknowsys.served.utils._
// import com.verknowsys.served.api._

// import java.io.FileNotFoundException
// import scala.io._
// import org.json4s._
// import org.json4s.native.JsonMethods._


// /**
//  *
//  * class to automatically load default configs of given software
//  * it uses package object with implicit conversion from string to service configuration
//  */
// class SvdServiceConfigLoader(name: String) extends Logging with SvdUtils {

//     implicit val formats = DefaultFormats // Brings in default date formats etc.

//     log.trace("SvdServiceConfigLoader: %s".format(name))

//     val fullName = SvdConfig.defaultSoftwareTemplatesDir / name + SvdConfig.defaultSoftwareTemplateExt
//     val defaultTemplate = parse(Source.fromFile(SvdConfig.defaultSoftwareTemplate + SvdConfig.defaultSoftwareTemplateExt).mkString) //.extract[SvdServiceConfig]
//     val appSpecificTemplate = parse(
//         try {
//             log.trace(s"Trying to load file: ${fullName}")
//             Source.fromFile(fullName).mkString
//         } catch {
//             case x: FileNotFoundException => // template not exists in common igniter location, try at user space
//                 val rootIgniterPrefix = SvdConfig.systemHomeDir / SvdConfig.defaultUserIgnitersDir
//                 val rootIgniterName = rootIgniterPrefix / name + SvdConfig.defaultSoftwareTemplateExt
//                 try { // try root igniter
//                     Source.fromFile(rootIgniterName).mkString
//                 } catch {
//                     case x: FileNotFoundException => // standard template check
//                         val userSideIgniter = SvdConfig.userHomeDir / "%d".format(getUserUid) / SvdConfig.defaultUserIgnitersDir / name + SvdConfig.defaultSoftwareTemplateExt
//                         log.debug("Common igniter not found: %s. Trying again with: %s", fullName, userSideIgniter)
//                         try {
//                             Source.fromFile(userSideIgniter).mkString
//                         } catch {
//                             case x: Exception =>
//                                 val e = s"No igniter with such name found: ${x}"
//                                 log.error(e)
//                                 throw new Exception(e)
//                                 // throwException[Exception](s"${e}")
//                         }
//                 }

//             // case x: Exception => // template not exists
//                 // log.error("SvdServiceConfigLoader Failure with igniter: %s", name)
//         }
//     )
//     val appTemplateMerged = defaultTemplate merge appSpecificTemplate

//     // val svcName = (appSpecificTemplate \\ "name").extract[String]
//     log.debug(s"Extracted SvdServiceConfig from igniter: ${name}.")
//     // log.trace("Default template: %s".format(defaultTemplate))
//     // log.trace("App template: %s".format(appSpecificTemplate))
//     log.trace(s"Merged template: ${compact(render(appTemplateMerged))}")


//     def config = SvdServiceConfig( // OPTIMIZE: this should be done automatically
//         name = name,
//         softwareName = (appTemplateMerged \ "softwareName").extract[String],
//         autoRestart = (appTemplateMerged \ "autoRestart").extract[Boolean],
//         autoStart = (appTemplateMerged \ "autoStart").extract[Boolean],
//         reportAllErrors = (appTemplateMerged \ "reportAllErrors").extract[Boolean],
//         reportAllInfos = (appTemplateMerged \ "reportAllInfos").extract[Boolean],
//         reportAllDebugs = (appTemplateMerged \ "reportAllDebugs").extract[Boolean],
//         staticPort = (appTemplateMerged \ "staticPort").extract[Int],
//         watchPort = (appTemplateMerged \ "watchPort").extract[Boolean],
//         schedulerActions = (appTemplateMerged \ "schedulerActions").children.map {
//             children =>
//                 SvdSchedulerActions(
//                     cronEntry = (children \ "cronEntry").extract[String],
//                     shellCommands = (children \ "shellCommands").extract[List[String]],
//                     jvmCommands = (children \ "jvmCommands").extract[List[String]]
//                 )
//             },

//         install = SvdShellOperations(
//                 commands = (appTemplateMerged \ "install" \ "commands").extract[List[String]],
//                 expectOutput = (appTemplateMerged \ "install" \ "expectOutput").extract[List[String]],
//                 expectOutputTimeout = (appTemplateMerged \ "install" \ "expectOutputTimeout").extract[Int]
//             ),
//         configure = SvdShellOperations(
//                 commands = (appTemplateMerged \ "configure" \ "commands").extract[List[String]],
//                 expectOutput = (appTemplateMerged \ "configure" \ "expectOutput").extract[List[String]],
//                 expectOutputTimeout = (appTemplateMerged \ "configure" \ "expectOutputTimeout").extract[Int]
//             ),
//         start = SvdShellOperations(
//                 commands = (appTemplateMerged \ "start" \ "commands").extract[List[String]],
//                 expectOutput = (appTemplateMerged \ "start" \ "expectOutput").extract[List[String]],
//                 expectOutputTimeout = (appTemplateMerged \ "start" \ "expectOutputTimeout").extract[Int]
//             ),
//         afterStart = SvdShellOperations(
//                 commands = (appTemplateMerged \ "afterStart" \ "commands").extract[List[String]],
//                 expectOutput = (appTemplateMerged \ "afterStart" \ "expectOutput").extract[List[String]],
//                 expectOutputTimeout = (appTemplateMerged \ "afterStart" \ "expectOutputTimeout").extract[Int]
//             ),
//         stop = SvdShellOperations(
//                 commands = (appTemplateMerged \ "stop" \ "commands").extract[List[String]],
//                 expectOutput = (appTemplateMerged \ "stop" \ "expectOutput").extract[List[String]],
//                 expectOutputTimeout = (appTemplateMerged \ "stop" \ "expectOutputTimeout").extract[Int]
//             ),
//         afterStop = SvdShellOperations(
//                 commands = (appTemplateMerged \ "afterStop" \ "commands").extract[List[String]],
//                 expectOutput = (appTemplateMerged \ "afterStop" \ "expectOutput").extract[List[String]],
//                 expectOutputTimeout = (appTemplateMerged \ "afterStop" \ "expectOutputTimeout").extract[Int]
//             ),
//         reload = SvdShellOperations(
//                 commands = (appTemplateMerged \ "reload" \ "commands").extract[List[String]],
//                 expectOutput = (appTemplateMerged \ "reload" \ "expectOutput").extract[List[String]],
//                 expectOutputTimeout = (appTemplateMerged \ "reload" \ "expectOutputTimeout").extract[Int]
//             ),
//         validate = SvdShellOperations(
//                 commands = (appTemplateMerged \ "validate" \ "commands").extract[List[String]],
//                 expectOutput = (appTemplateMerged \ "validate" \ "expectOutput").extract[List[String]],
//                 expectOutputTimeout = (appTemplateMerged \ "validate" \ "expectOutputTimeout").extract[Int]
//             )
//         )

// }
