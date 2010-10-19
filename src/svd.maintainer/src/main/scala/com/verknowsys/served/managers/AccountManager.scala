package com.verknowsys.served.managers

import scala.actors.Actor
import com.verknowsys.served.utils.Utils

/**
 * Account Manager - owner of all managers
 * 
 * @author teamon
 */
class AccountManager(val account: Account) extends Actor with Utils {
    start
    
    def act {
        loop {
            receive {
                
            }
        }
    }

}