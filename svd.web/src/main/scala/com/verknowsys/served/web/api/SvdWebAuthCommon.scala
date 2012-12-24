package com.verknowsys.served.web.api


import unfiltered.response._

import com.verknowsys.served.api._
import com.verknowsys.served.utils._

import akka.actor._
import akka.pattern.ask
import unfiltered._
import unfiltered.filter.Plan
import unfiltered.Cookie


/**
 *  @author dmilith
 *
 *  Common code for Web API.
 */
abstract class SvdWebAPI(webManager: ActorRef) extends Plan with Logging with SvdUtils {


    /**
     * @author Daniel (dmilith) Dettlaff
     *
     *  Cookies auth method simple implementation.
     *
     */
    def checkAuth(cookies: Map[String, Option[Cookie]])(block: => ResponseFunction[Any]) = {
        cookies("svdauth") match {
            case Some(Cookie(_, pref, _, _, _, _, _, _)) =>
                log.debug("Auth cookie found.")
                log.warn("AUTH NOT YET IMPLEMENTED. ANY VALUE OF svdauth cookie will pass auth")
                block

            case _ =>
                log.debug("No svd auth cookie.")
                SvdWebAPI.apiRespond(webManager ? Error("Unauthorized. Use /Authorize/key first."))
        }
    }


    def intent: Cycle.Intent[Any,Any]


}

