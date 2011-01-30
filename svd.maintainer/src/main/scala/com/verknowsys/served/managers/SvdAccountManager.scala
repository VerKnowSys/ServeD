package com.verknowsys.served.managers

import com.verknowsys.served.maintainer.SvdAccount
import com.verknowsys.served.utils.SvdExceptionHandler
import akka.actor.Actor
import akka.util.Logging

case object GetAccount

/**
 * Account Manager - owner of all managers
 * 
 * @author teamon
 */
class SvdAccountManager(val account: SvdAccount) extends Actor with Logging with SvdExceptionHandler {
    log.trace("Starting AccountManager for account: " + account)
    
    def receive = {
        case GetAccount => self reply account
        case msg => log.warn("Message net recoginzed: %s", msg)
    }

}
