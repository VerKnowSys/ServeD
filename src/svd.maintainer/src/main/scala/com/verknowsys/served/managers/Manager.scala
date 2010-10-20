package com.verknowsys.served.managers

import scala.actors.Actor
import com.verknowsys.served.utils.Utils
import com.verknowsys.served.utils.monitor._

/**
 * Base class for all managers
 * 
 * @author teamon
 */
abstract class Manager(owner: AccountManager) extends Actor with Utils with MonitoredActor {
    start
    
    protected def account = owner.account
}