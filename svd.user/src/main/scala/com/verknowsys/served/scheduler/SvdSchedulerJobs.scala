// /*
//  * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
//  * This Software is a close code project. You may not redistribute this code without permission of author.
//  */

// package com.verknowsys.served.scheduler


// import com.verknowsys.served._
// import com.verknowsys.served.api._
// import com.verknowsys.served.utils._
// import com.verknowsys.served.systemmanager.native._
// import org.quartz.{JobExecutionContext, Job}


// class TimeSynchronizeJob extends Job with Logging {

//     def execute(context: JobExecutionContext) = {
//         log.trace("Quartz -> Executing Job of TimeSynchronizeJob")
//         SvdNtpSync()
//     }

// }


// class ShellJob extends Job with Logging {
//     def execute(context: JobExecutionContext) = {
//         log.trace("Quartz -> Executing Job of ShellJob")
//         val data = context.getJobDetail.getJobDataMap

//         val operations = data.get("shellOperations").asInstanceOf[SvdShellOperations]
//         val account = data.get("account").asInstanceOf[SvdAccount]

//         val shell = new SvdShell(account, SvdConfig.defaultSchedulerShellTimeout / 1000)
//         log.debug(s"Launching ShellJob: ${operations.commands.mkString(", ")}")
//         shell.exec(operations)
//         shell.close
//     }
// }

