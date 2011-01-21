package com.verknowsys.served.managers

import com.verknowsys.served.maintainer.Account
import akka.actor.Actor
import akka.util.Logging

case object GetAccount

/**
 * Account Manager - owner of all managers
 * 
 * @author teamon
 */
class AccountManager(val account: Account) extends Actor with Logging {
    log.trace("Starting AccountManager for account: " + account)
    
    def receive = {
        case GetAccount => self reply account
        case msg => log.warn("Message net recoginzed: %s", msg)
    }

}
