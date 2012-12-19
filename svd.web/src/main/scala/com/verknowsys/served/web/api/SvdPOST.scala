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
import unfiltered.jetty.Http
import java.net.URL
import unfiltered.filter.Plan
import akka.actor._
import akka.dispatch._
import akka.util.Timeout
import akka.util.duration._
import akka.pattern.ask
import unfiltered.Cookie
import unfiltered.Cookie._


/**
 *  @author dmilith
 *
 *  Web POST API is used to perform communication with web manager
 */
class SvdPOST(webManager: ActorRef, account: SvdAccount, webPort: Int) extends SvdWebAPI(webManager) {

    import org.json4s._
    import org.json4s.native._
    import org.json4s.JsonDSL._
    import webImplicits._


    implicit val timeout = Timeout(SvdConfig.defaultAPITimeout/1000 seconds)


    def intent = {

        /** API POST call #000  */
        case req @ POST(Path("/Authorize") & Params(params)) =>
            log.debug("POST on Authorize")
            log.trace("XXX: for dmilith: %s".format(sha1("dmilith"))) // XXX
            log.trace("XXX: for tallica: %s".format(sha1("tallica"))) // XXX
            log.debug("PARAMS: %s".format(params.mkString))

            def param(key: String) = params.get(key).flatMap { _.headOption } getOrElse("")
            val key = param("Authorize")
            log.trace("XXX: given: %s".format(key)) // XXX

            if ((key == sha1("dmilith")) || (key == sha1("tallica"))) // FIXME: XXX: TODO: hardcode auth key
                JsonContent ~> SetCookies(Cookie("svdauth", key, maxAge = Some(3600*24))) ~>
                    ResponseString("""{"message": "Authorized successfully.", "status": 0}""")
            else
                JsonContent ~> Unauthorized ~>
                    ResponseString("""{"message": "Authorization failed.", "status": 5}""")


        /** API POST call #001  */
        case req @ POST(Path("/GetUserProcesses") & Cookies(cookies)) =>
            checkAuth(cookies) {
                log.debug("POST on GetUserProcesses")
                SvdWebAPI.apiRespond(webManager ? System.GetUserProcesses(account.uid))
            }


        /** API POST call #002  */
        case req @ POST(Path("/RegisterDomain") & Params(params) & Cookies(cookies)) =>
            checkAuth(cookies) {
                def param(key: String) = params.get(key).flatMap { _.headOption } getOrElse("")

                log.debug("POST /RegisterDomain from form params")
                param("RegisterDomain") match {
                    case domain: String =>
                        log.debug("Given domain: %s", domain)
                        SvdWebAPI.apiRespond(webManager ? System.RegisterDomain(domain, webManager))

                    case _ =>
                        JsonContent ~> ResponseString("{\"message\": \"Invalid API request.\", \"status\":3}")
                }
            }


        /** API POST call #003  */
        case req @ POST(Path("/RegisteredDomains") & Cookies(cookies)) =>
            checkAuth(cookies) {
                SvdWebAPI.apiRespond(webManager ? User.RegisteredDomains)
            }


        /** API POST call #004  */
        case req @ POST(Path("/GetStoredServices") & Cookies(cookies)) =>
            checkAuth(cookies) {
                SvdWebAPI.apiRespond(webManager ? User.GetStoredServices)
            }

        /** API POST call #005  */
        case req @ POST(Path("/TerminateServices") & Cookies(cookies)) =>
            checkAuth(cookies) {
                SvdWebAPI.apiRespond(webManager ? User.TerminateServices)
            }

        /** API POST call #006  */
        case req @ POST(Path("/StoreServices") & Cookies(cookies)) =>
            checkAuth(cookies) {
                SvdWebAPI.apiRespond(webManager ? User.StoreServices)
            }

        /** API POST call #007  */
        case req @ POST(Path("/SpawnService") & Params(params) & Cookies(cookies)) =>
            checkAuth(cookies) {
                def param(key: String) = params.get(key).flatMap { _.headOption } getOrElse("")
                SvdWebAPI.apiRespond(webManager ? User.SpawnService(param("SpawnService")))
            }

        /** API POST call #008  */
        case req @ POST(Path("/TerminateService") & Params(params) & Cookies(cookies)) =>
            checkAuth(cookies) {
                def param(key: String) = params.get(key).flatMap { _.headOption } getOrElse("")
                SvdWebAPI.apiRespond(webManager ? User.TerminateService(param("TerminateService")))
            }

        /** API POST call #009  */
        case req @ POST(Path("/ShowAvailableServices") & Cookies(cookies)) =>
            checkAuth(cookies) {
                SvdWebAPI.apiRespond(webManager ? User.ShowAvailableServices)
            }

        /** API POST call #010  */
        case req @ POST(Path("/SpawnServices") & Cookies(cookies)) =>
            checkAuth(cookies) {
                SvdWebAPI.apiRespond(webManager ? User.SpawnServices)
            }

        /** API POST call #011  */
        case req @ POST(Path("/GetServiceStatus") & Params(params) & Cookies(cookies)) =>
            checkAuth(cookies) {
                def param(key: String) = params.get(key).flatMap { _.headOption } getOrElse("")
                SvdWebAPI.apiRespond(webManager ? User.GetServiceStatus(param("GetServiceStatus")))
            }

        /** API POST call #012  */
        case req @ POST(Path("/GetServicePort") & Params(params) & Cookies(cookies)) =>
            checkAuth(cookies) {
                def param(key: String) = params.get(key).flatMap { _.headOption } getOrElse("")
                SvdWebAPI.apiRespond(webManager ? User.GetServicePort(param("GetServicePort")))
            }

        /** API POST call #013  */
        case req @ POST(Path("/CloneIgniterForUser") & Params(params) & Cookies(cookies)) =>
            checkAuth(cookies) {
                def param(key: String) = params.get(key).flatMap { _.headOption } getOrElse("")
                SvdWebAPI.apiRespond(webManager ? User.CloneIgniterForUser(
                    param("IgniterName"), Some(param("UserIgniterName"))))
            }

        /** API POST call #014  */
        case req @ POST(Path("/RegisterAccount") & Params(params) & Cookies(cookies)) =>
            def param(key: String) = params.get(key).flatMap { _.headOption } getOrElse("")
            SvdWebAPI.apiRespond(webManager ? Admin.RegisterAccount(param("RegisterAccount")))


        /** API POST call #DEFAULT  */
        case req @ POST(_) =>
            JsonContent ~> ResponseString("{\"message\": \"Invalid API request.\", \"status\":3}")

    }

}
