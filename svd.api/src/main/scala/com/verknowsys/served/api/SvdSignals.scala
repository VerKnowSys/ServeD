// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.api

/**
 * User: dmilith
 * Date: Jun 30, 2009
 * Time: 11:53:57 PM
 */

case object Init
case object Run
case object Reload
case object Quit
case object Ready
case object MainLoop
case object ProcessMessages
case object RemoteBackupDone
case object RevertBackup

// Internal response
// sealed abstract class InternalResponse
// case object Success extends InternalResponse
// case object Failure extends InternalResponse

// SvdSystemManager
case object GetRunningProcesses
case object GetNetstat

case class GetUserProcesses(uid: Int)
case class SpawnProcess(cmd: String)
case class KillProcess(what: Int, signal: Any)
case class Chmod(what: String, mode: Int, recursive: Boolean)
case class Chown(what: String, userId: Int, recursive: Boolean)
