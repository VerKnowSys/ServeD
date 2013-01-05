/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

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
// case object ApiSuccess extends InternalResponse
// case object Failure extends InternalResponse
