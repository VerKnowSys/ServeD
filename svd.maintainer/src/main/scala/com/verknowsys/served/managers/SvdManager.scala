package com.verknowsys.served.managers

import akka.actor.Actor
import com.verknowsys.served.utils.SvdExceptionHandler
import com.verknowsys.served.maintainer.SvdAccount


/**
 * Base class for all managers
 * 
 * @author teamon
 */
abstract class SvdManager(account: SvdAccount) extends Actor with SvdExceptionHandler {

}