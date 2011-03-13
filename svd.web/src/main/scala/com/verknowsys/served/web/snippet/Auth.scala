package com.verknowsys.served.web.snippet

import net.liftweb.util.Helpers._
import net.liftweb.http._
import net.liftweb.common.{Full, Box}
import xml.{Text, NodeSeq}

import com.verknowsys.served.web.lib.Session

class Auth {
    def login(xhtml: NodeSeq) = {
        object username extends RequestVar("")
        var password = ""
        
        bind("f", xhtml,
            "username" -%> SHtml.text(username.get, username.set),
            "password" -%> SHtml.password("", password = _),
            "send" -%> SHtml.submit("Send!", () => {
                if(Session.login(username.get, password)){
                    S.redirectTo("/")
                } else {
                    S.error("Wrong usernmae")
                }
            })
        )
    }
}
