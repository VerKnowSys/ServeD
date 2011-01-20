package com.verknowsys.served.managers

import akka.actor.Actor
/**
 * Base class for all managers
 * 
 * @author teamon
 */
abstract class Manager(owner: AccountManager) extends Actor {
    
    
    protected def account = owner.account
}