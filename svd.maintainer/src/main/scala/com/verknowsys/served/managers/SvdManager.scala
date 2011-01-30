package com.verknowsys.served.managers

import akka.actor.Actor
import com.verknowsys.served.utils._


/**
 * Base class for all managers
 * 
 * @author teamon
 */
abstract class SvdManager(owner: SvdAccountManager) extends Actor with SvdExceptionHandler {
    
    
    protected def account = owner.account
}