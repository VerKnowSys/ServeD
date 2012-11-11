package com.verknowsys.served.managers

import com.verknowsys.served.utils._
import com.verknowsys.served.api._
import akka.actor.Actor


/**
 * Base trait for all managers
 *
 * @author teamon
 */
abstract trait SvdManager extends Actor with SvdActor with Logging with SvdUtils
