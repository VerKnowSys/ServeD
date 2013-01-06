/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.api

/**
 *  Defines signals to be used in Process/ Service managment API.
 * @author dmilith
 */

object Signal {

    abstract class Base extends ApiMessage

    case object Run extends Base
    case object Reload extends Base
    case object Quit extends Base

    // case object Init extends Base
    // case object Ready extends Base
    // case object MainLoop extends Base
    // case object ProcessMessages extends Base
    // case object RemoteBackupDone extends Base
    // case object RevertBackup extends Base
}

// Internal response
// sealed abstract class InternalResponse
// case object ApiSuccess extends InternalResponse
// case object Failure extends InternalResponse
