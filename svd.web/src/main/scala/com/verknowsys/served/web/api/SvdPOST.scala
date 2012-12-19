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
        case req @ POST(Path(Seg("Authorize" :: key :: Nil)) & Cookies(cookies)) =>
            log.debug("POST on Authorize")
            log.trace("XXX: for dmilith: %s".format(sha1("dmilith"))) // XXX
            log.trace("XXX: for tallica: %s".format(sha1("tallica"))) // XXX
            log.trace("XXX: given: %s".format(key)) // XXX
            if ((key == sha1("dmilith")) || (key == sha1("tallica"))) // FIXME: XXX: TODO: hardcode auth key
                SetCookies(Cookie("svdauth", key, maxAge = Some(3600*24))) ~>
                    ResponseString("""{"message": "Authorized successfully.", "status": 0}""")
            else
                Unauthorized ~>
                    ResponseString("""{"message": "Authorization failed.", "status": 5}""")


        /** API POST call #001  */
        case req @ POST(Path(Seg("GetUserProcesses" :: Nil)) & Cookies(cookies)) =>
            checkAuth(cookies) {
                log.debug("POST on GetUserProcesses")
                SvdWebAPI.apiRespond(webManager ? System.GetUserProcesses(account.uid))
            }


        /** API POST call #002  */
        case req @ POST(Path(Seg("RegisterDomain" :: domain :: Nil)) & Cookies(cookies)) =>
            checkAuth(cookies) {
                log.debug("POST /RegisterDomain by path")
                log.info("Given domain to be registered: %s", domain)
                SvdWebAPI.apiRespond(webManager ? System.RegisterDomain(domain, webManager))
            }

        case req @ POST(Path(Seg("RegisterDomain" :: Nil)) & Params(params) & Cookies(cookies)) =>
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
        case req @ POST(Path(Seg("RegisteredDomains" :: Nil)) & Cookies(cookies)) =>
            checkAuth(cookies) {
                SvdWebAPI.apiRespond(webManager ? User.RegisteredDomains)
            }


        /** API POST call #004  */
        case req @ POST(Path(Seg("GetStoredServices" :: Nil)) & Cookies(cookies)) =>
            checkAuth(cookies) {
                SvdWebAPI.apiRespond(webManager ? User.GetStoredServices)
            }

        /** API POST call #005  */
        case req @ POST(Path(Seg("TerminateServices" :: Nil)) & Cookies(cookies)) =>
            checkAuth(cookies) {
                SvdWebAPI.apiRespond(webManager ? User.TerminateServices)
            }

        /** API POST call #006  */
        case req @ POST(Path(Seg("StoreServices" :: Nil)) & Cookies(cookies)) =>
            checkAuth(cookies) {
                SvdWebAPI.apiRespond(webManager ? User.StoreServices)
            }

        /** API POST call #007  */
        case req @ POST(Path(Seg("SpawnService" :: serviceName :: Nil)) & Cookies(cookies)) =>
            checkAuth(cookies) {
                SvdWebAPI.apiRespond(webManager ? User.SpawnService(serviceName))
            }

        /** API POST call #008  */
        case req @ POST(Path(Seg("TerminateService" :: serviceName :: Nil)) & Cookies(cookies)) =>
            checkAuth(cookies) {
                SvdWebAPI.apiRespond(webManager ? User.TerminateService(serviceName))
            }

        /** API POST call #009  */
        case req @ POST(Path(Seg("ShowAvailableServices" :: Nil)) & Cookies(cookies)) =>
            checkAuth(cookies) {
                SvdWebAPI.apiRespond(webManager ? User.ShowAvailableServices)
            }

        /** API POST call #010  */
        case req @ POST(Path(Seg("SpawnServices" :: Nil)) & Cookies(cookies)) =>
            checkAuth(cookies) {
                SvdWebAPI.apiRespond(webManager ? User.SpawnServices)
            }

        /** API POST call #011  */
        case req @ POST(Path(Seg("GetServiceStatus" :: name :: Nil)) & Cookies(cookies)) =>
            checkAuth(cookies) {
                SvdWebAPI.apiRespond(webManager ? User.GetServiceStatus(name))
            }

        /** API POST call #012  */
        case req @ POST(Path(Seg("GetServicePort" :: number :: Nil)) & Cookies(cookies)) =>
            checkAuth(cookies) {
                SvdWebAPI.apiRespond(webManager ? User.GetServicePort(number))
            }

        /** API POST call #013  */
        case req @ POST(Path(Seg("CloneIgniterForUser" :: igniterName :: userIgniterName :: Nil)) & Cookies(cookies)) =>
            checkAuth(cookies) {
                SvdWebAPI.apiRespond(webManager ? User.CloneIgniterForUser(igniterName, Some(userIgniterName)))
            }

        /** API POST call #014  */
        case req @ POST(Path(Seg("RegisterAccount" :: accountName :: Nil)) & Cookies(cookies)) =>
            SvdWebAPI.apiRespond(webManager ? Admin.RegisterAccount(accountName))


        /** API POST call #DEFAULT  */
        case req @ POST(_) =>
            JsonContent ~> ResponseString("{\"message\": \"Invalid API request.\", \"status\":3}")

    }

}
