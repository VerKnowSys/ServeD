/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.web.api


import unfiltered.request._
import unfiltered.response._

import com.verknowsys.served._
import com.verknowsys.served.api._
import com.verknowsys.served.utils._

import akka.actor._
import akka.util.Timeout
import akka.util.duration._


/**
 *  @author dmilith
 *
 *  Web GET API is used to perform communication with web manager
 */
class SvdGET(webManager: ActorRef, account: SvdAccount, webPort: Int) extends SvdWebAPI(webManager) {


    implicit val timeout = Timeout(SvdConfig.defaultAPITimeout/1000 seconds)


    def layout(content: scala.xml.NodeBuffer) = Html(
        <html>
            <head>
                <title>ServeD</title>
                <meta http_equiv="Content-Type" content="text/html; charset=utf-8"/>
                <link rel="stylesheet" type="text/css" href="/assets/css/bootstrap.min.css"/>
                <link rel="stylesheet" type="text/css" href="/assets/css/main.css"/>
            </head>
            <body>
                <script type="text/javascript" src="/assets/js/jquery-1.8.3.min.js"/>
                <script type="text/javascript" src="/assets/js/bootstrap.min.js"/>
                <script type="text/javascript" src="/assets/js/dough.min.js"/>
                <script type="text/javascript" src="/assets/js/main.js"/>
                { content }
            </body>
        </html>
    )


    def intent = {

        /** API GET call #001  */
        case req @ GET(Path(Seg("Header" :: Nil))) =>
            Ok ~> Html(
                <h1>{ Dict("User Panel") }</h1>
                <p>{ Dict("Welcome") + " " + account.userName }</p>
                <p>{ Dict("Details") + ": " + account }</p>)


        /** API GET call #002  */
        case req @ GET(Path(Seg("ProcList" :: Nil))) =>
            Ok ~> Html(
                <script type="text/javascript" src="/assets/js/raphael-min.js"/>
                <script type="text/javascript" src="/assets/js/g.graphael.js"/>
                <script type="text/javascript" src="/assets/js/g.dot.js"/>
                <script type="text/javascript" src="/assets/js/proclist.js"/>
                <article>
                  <header>ProcList</header>
                  <div id="holder">Cos</div>
                </article>
            )


        /** API GET call #003  */
        case req @ GET(Path(Seg("AdminPanel" :: Nil))) =>
            Ok ~> Html(
                <div class="services_result">Result should be shown here</div>
                <div class="services_data">Data should be shown here</div>

                <article class="services">
                    <div class="authorize">Authorize as dmilith</div>
                    <div class="get_user_processes">GetUserProcesses</div>
                    <div class="get_stored_services">GetStoredServices</div>
                    <div class="register_domain">RegisterDomain</div>
                    <div class="registered_domains">RegisteredDomains</div>
                    <div class="terminate_services">TerminateServices</div>
                    <div class="store_services">StoreServices</div>
                    <div class="spawn_service">SpawnService</div>
                    <div class="terminate_service">TerminateService</div>
                    <div class="show_available_services">ShowAvailableServices</div>
                    <div class="spawn_services">SpawnServices</div>
                    <div class="get_service_status">GetServiceStatus</div>
                    <div class="clone_igniter_for_user">CloneIgniterForUser</div>
                    <div class="register_account">RegisterAccount</div>
                    <div class="get_service_port">GetServicePort</div>
                    <div class="create_file_watch">CreateFileWatch</div>
                    <div class="create_file_watch1">CreateFileWatch1</div>
                    <div class="create_file_watch2">CreateFileWatch2</div>
                    <div class="create_file_watch3">CreateFileWatch3</div>
                    <div class="destroy_file_watch">DestroyFileWatch</div>
                    <div class="destroy_file_watch1">DestroyFileWatch1</div>
                    <div class="destroy_file_watch2">DestroyFileWatch2</div>
                    <div class="destroy_file_watch3">DestroyFileWatch3</div>
                </article>
            )



        /** API GET call #DEFAULT  */
        case req @ _ =>
            log.debug("GET /")
            Ok ~> layout(
                <section class="header"></section>
                <section class="content">
                    <div class="target">Connection not estabilished yet.</div>
                    <div class="messages">No data yet.</div>
                    <section class="admin"></section>
                    <section class="pslist"></section>
                </section>
            )

    }

}