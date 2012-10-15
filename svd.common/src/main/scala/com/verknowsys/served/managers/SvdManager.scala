package com.verknowsys.served.managers

import com.verknowsys.served.utils._
import com.verknowsys.served.api._
import akka.actor.Actor


/**
 * Base trait for all managers
 *
 * @author teamon
 */
abstract trait SvdManager extends Actor with SvdExceptionHandler with Logging with SvdUtils
