package com.verknowsys.served.web.snippet

import net.liftweb.util._
import net.liftweb.util.Helpers._
import net.liftweb.common._
import java.util.Date

class HelloWorld {
    def howdy = ".time" #> Helpers.formattedTimeNow 
}
