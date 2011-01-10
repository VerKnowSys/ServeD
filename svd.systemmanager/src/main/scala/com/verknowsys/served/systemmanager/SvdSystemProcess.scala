package com.verknowsys.served.systemmanager


import com.verknowsys.served.utils._
import com.verknowsys.served.utils.signals._

import com.sun.jna.{Native, Library}
import scala.actors.Actor
import scala.actors.Actor._


/**
  * This class defines mechanism which system commands will be executed through (and hopefully monitored)
  *
  * @author dmilith
  */

// object SvdSystemProcessState extends Enumeration(initial = 0) {
//     
//     type SvdSystemProcessState = Value
//     val IDLE, RUNNING, WAITING = Value
//     
// }


class SvdSystemProcess(val commandInput: String = "") extends Actor with Utils {
    // import SvdSystemProcessState._
    
    start
    Native.setProtected(true)
    
    def act {
        loop {
            react {
                case Init =>
                    logger.debug("new SystemProcess(%s)".format(commandInput))
                    val results = Exec.noBlockCommand(commandInput)
                    logger.debug("new SystemProcess(%s) results: %s %d".format(commandInput, results._1, results._2))
                    act
                
                case Quit =>
                    logger.info("SystemProcess(%s) ended.".format(commandInput))
                    act
                case x: Any =>
                    logger.info("Request for unsupported signal of SystemProcess: %s".format(x.toString))
                    act
            }
        }
    }
    
}