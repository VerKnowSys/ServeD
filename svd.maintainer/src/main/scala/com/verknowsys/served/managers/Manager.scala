package com.verknowsys.served.managers

import scala.actors.Actor
import com.verknowsys.served.utils.CommonActor
import com.verknowsys.served.utils.monitor.Monitored

/**
 * Base class for all managers
 * 
 * @author teamon
 */
abstract class Manager(owner: AccountManager) extends CommonActor with Monitored {
    start
    
    protected def account = owner.account
}