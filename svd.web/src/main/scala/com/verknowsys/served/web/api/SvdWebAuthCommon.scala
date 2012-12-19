package com.verknowsys.served.web.api


import unfiltered.Cookie
import unfiltered.request._
import unfiltered.response._
import unfiltered.kit._
import unfiltered.filter._
import org.json4s._
import org.json4s.native._
import java.util.UUID

import com.verknowsys.served._
import com.verknowsys.served.api._
import com.verknowsys.served.utils._
import com.verknowsys.served.web._
import com.verknowsys.served.web.api._
import com.verknowsys.served.db.{DBServer, DBClient, DB}

import javax.servlet.http.HttpServletResponse
import java.net.URL
import akka.actor._
import akka.dispatch._
import akka.util.Timeout
import akka.util.duration._
import akka.pattern.ask
import unfiltered._
import unfiltered.filter.Plan
import unfiltered.jetty.Http
import unfiltered.Cookie
import unfiltered.Cookie._


/**
 *  @author dmilith
 *
 *  Common code for Web API.
 */
abstract class SvdWebAPI(webManager: ActorRef) extends Plan with Logging with SvdUtils {


    import webImplicits._


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

