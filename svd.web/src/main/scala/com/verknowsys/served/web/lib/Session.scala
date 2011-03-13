package com.verknowsys.served.web.lib

import net.liftweb.http.SessionVar

object Session {
    object Username extends SessionVar[String]("")

    def login(username: String, password: String) = {
        Username.set(username)
        authorized
    }
    
    def logout = Username.set("")
    
    def authorized = Username.get != "" // TODO: Change me please

}
