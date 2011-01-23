package com.verknowsys.served.managers

import akka.actor.Actor
/**
 * Base class for all managers
 * 
 * @author teamon
 */
abstract class SvdManager(owner: SvdAccountManager) extends Actor {
    
    
    protected def account = owner.account
}