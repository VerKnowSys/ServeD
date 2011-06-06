package com.verknowsys.served.systemmanager.managers


import com.verknowsys.served.utils.SvdExceptionHandler
import com.verknowsys.served.systemmanager.native._

import akka.actor.Actor


/**
 * Base class for all managers
 * 
 * @author teamon
 */
abstract class SvdManager(account: SvdAccount) extends Actor with SvdExceptionHandler {

}