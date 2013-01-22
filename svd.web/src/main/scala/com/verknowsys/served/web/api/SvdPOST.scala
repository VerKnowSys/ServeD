/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.web.api


import unfiltered.request._
import unfiltered.response._

import com.verknowsys.served._
import com.verknowsys.served.api._

import akka.actor._
import akka.util.Timeout
import scala.concurrent.duration._
import akka.pattern.ask
import unfiltered.Cookie


/**
 *  @author dmilith
 *
 *  Web POST API is used to perform communication with web manager
 */
class SvdPOST(webManager: ActorRef, account: SvdAccount, webPort: Int) extends SvdWebAPI(webManager) {


    implicit val timeout = Timeout(SvdConfig.defaultAPITimeout/1000 seconds)


    def intent = {

        /** API POST call #000  */
        case req @ POST(Path("/Authorize")) =>
            log.debug("POST on Authorize")

            if (true) // rotfl
                JsonContent ~> SetCookies(Cookie("svdauth", "kluczo!", maxAge = Some(3600*24))) ~>
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
        case req @ POST(Path("/GetRegisteredDomains") & Cookies(cookies)) =>
            checkAuth(cookies) {
                SvdWebAPI.apiRespond(webManager ? User.GetRegisteredDomains)
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

        /** API POST call #015  */
        case req @ POST(Path("/CreateFileWatch") & Params(params) & Cookies(cookies)) =>
            checkAuth(cookies) {
                def param(key: String) = params.get(key).flatMap { _.headOption } getOrElse("")
                SvdWebAPI.apiRespond(webManager ? User.CreateFileWatch(param("CreateFileWatch"), param("Flags").toInt, param("ServiceName")))
            }

        /** API POST call #016  */
        case req @ POST(Path("/DestroyFileWatch") & Params(params) & Cookies(cookies)) =>
            checkAuth(cookies) {
                def param(key: String) = params.get(key).flatMap { _.headOption } getOrElse("")
                SvdWebAPI.apiRespond(webManager ? User.DestroyFileWatch(param("DestroyFileWatch")))
            }

        /** API POST call #017  */
        case req @ POST(Path("/GetAccountPriviledges") & Cookies(cookies)) =>
            checkAuth(cookies) {
                SvdWebAPI.apiRespond(webManager ? Security.GetAccountPriviledges(account))
            }

        /** API POST call #018  */
        case req @ POST(Path("/RestartAccountManager") & Cookies(cookies)) =>
            checkAuth(cookies) {
                SvdWebAPI.apiRespond(webManager ? Maintenance.RestartAccountManager)
            }

        /** API POST call #019  */
        case req @ POST(Path("/GetUserPorts") & Cookies(cookies)) =>
            checkAuth(cookies) {
                SvdWebAPI.apiRespond(webManager ? User.GetUserPorts)
            }

        /** API POST call #020  */
        case req @ POST(Path("/RemoveAllReservedPorts") & Cookies(cookies)) =>
            checkAuth(cookies) {
                SvdWebAPI.apiRespond(webManager ? User.RemoveAllUserPorts)
            }

        /** API POST call #021  */
        case req @ POST(Path("/RegisterUserPort") & Cookies(cookies)) =>
            checkAuth(cookies) {
                SvdWebAPI.apiRespond(webManager ? User.RegisterUserPort)
            }

        /** API POST call #022  */
        case req @ POST(Path("/MoshAuth") & Cookies(cookies)) =>
            checkAuth(cookies) {
                SvdWebAPI.apiRespond(webManager ? User.MoshAuth)
            }

        /** API POST call #DEFAULT  */
        case req @ POST(_) =>
            JsonContent ~> ResponseString("{\"message\": \"Invalid API request.\", \"status\":3}")

    }

}
