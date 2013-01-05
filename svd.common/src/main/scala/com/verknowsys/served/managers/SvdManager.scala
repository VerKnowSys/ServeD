/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.managers

import com.verknowsys.served.utils._
import akka.actor.Actor


/**
 * Base trait for all managers
 *
 * @author teamon
 */
abstract trait SvdManager extends Actor with SvdActor with Logging with SvdUtils
