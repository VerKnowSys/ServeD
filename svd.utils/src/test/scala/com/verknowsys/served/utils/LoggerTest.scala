package com.verknowsys.served.utils


import org.specs._

class A extends SvdLogged {
   logger.debug("dupa")
}


class LoggerTest extends Specification {
    "Logger" should {
        
        "log!" in {
   
            val a = new A
        }
        
    }
}
