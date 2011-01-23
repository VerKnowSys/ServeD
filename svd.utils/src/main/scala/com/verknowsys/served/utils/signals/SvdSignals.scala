// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.utils.signals

/**
 * User: dmilith
 * Date: Jun 30, 2009
 * Time: 11:53:57 PM
 */

case object Init
case object Run
case object Quit
case object Ready
case object MainLoop
case object ProcessMessages
case object RemoteBackupDone
case object RevertBackup

// Internal response
sealed abstract class InternalResponse
case object Success extends InternalResponse
case object Failure extends InternalResponse

// SvdSystemManager
case object GetAllProcesses
case object GetRunningProcesses

case class SpawnProcess(cmd: String)
case class Kill(what: Int, signal: Any)
