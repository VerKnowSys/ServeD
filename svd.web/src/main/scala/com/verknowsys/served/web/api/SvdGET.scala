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
import scala.concurrent.duration._


/**
 *  @author dmilith
 *
 *  Web GET API is used to perform communication with web manager
 */
class SvdGET(webManager: ActorRef, account: SvdAccount, webPort: Int) extends SvdWebAPI(webManager) {


    implicit val timeout = Timeout(SvdConfig.defaultAPITimeout/1000 seconds)


    def layout(content: scala.xml.NodeBuffer) = Html5(
        <html>
            <head>
                <title>ServeD</title>
                <link rel="stylesheet" type="text/css" href="/assets/css/bootstrap.min.css"/>
                <link rel="stylesheet" type="text/css" href="/assets/css/font-awesome.min.css"/>
                <link rel="stylesheet" type="text/css" href="/assets/css/main.css"/>
            </head>
            <body>
                <script type="text/javascript" src="/assets/js/jquery.min.js"></script>
                <script type="text/javascript" src="/assets/js/bootstrap.min.js"></script>
                <script type="text/javascript" src="/assets/js/dough.min.js"></script>
                <script type="text/javascript" src="/assets/js/main.js"></script>
                { content }
            </body>
        </html>
    )


    def raphaelDeps(content: scala.xml.NodeBuffer) = Html5(
        <div>
            <script type="text/javascript" src="/assets/js/raphael-min.js"></script>
            <script type="text/javascript" src="/assets/js/g.graphael.js"></script>
            <script type="text/javascript" src="/assets/js/g.dot.js"></script>
            { content }
        </div>
    )



    def intent = {

        /** API GET call #000  */
        case req @ GET(Path(Seg("Header" :: Nil))) =>
            Ok ~> Html5(
                <h1>{ Dict("User Panel") }</h1>
                <p>{ Dict("Welcome") + " " + account.userName }</p>
                <p>{ Dict("Details") + ": " + account }</p>
            )


        /** API GET call #001  */
        case req @ GET(Path(Seg("ProcessList" :: Nil))) =>
            Ok ~> raphaelDeps(
                <script type="text/javascript" src="/assets/js/proclist.js"></script>
                <article>
                  <header>ProcessList</header>
                  <div class="holder">ProcessList</div>
                </article>
            )


        /** API GET call #002  */
        case req @ GET(Path(Seg("ServiceList" :: Nil))) =>
            Ok ~> raphaelDeps(
                <script type="text/javascript" src="/assets/js/serviceStandardLayout.js"></script>
                <script type="text/javascript" src="/assets/js/serviceLayout.js"></script>
                <script type="text/javascript" src="/assets/js/serviceInfoLayout.js"></script>
                <script type="text/javascript" src="/assets/js/servlist.js"></script>
                <article>
                  <header>ServiceList</header>
                  <div id="raw_service_layout"></div>
                </article>
            )


        /** API GET call #004  */
        case req @ GET(Path(Seg("AdminPanel" :: Nil))) =>
            Ok ~> Html5(
                <article class="results">
                    <div class="services_result">Result should be shown here</div>
                    <div class="services_data">Data should be shown here</div>
                </article>

                <article class="services">
                    <header>Services</header>
                    <div class="get_user_processes icon-tasks clickicon" title="Get processes list"></div>
                    <div class="get_stored_services icon-building clickicon" title="Get stored services"></div>
                    <div class="terminate_services icon-remove-circle clickicon" title="Terminate all currently running services"></div>
                    <div class="store_services icon-sitemap clickicon" title="Store all running services"></div>
                    <div class="spawn_service icon-off clickicon" title="Spawn this service"></div>
                    <div class="terminate_service icon-remove clickicon" title="Terminate this service"></div>
                    <div class="show_available_services icon-reorder clickicon" title="Show available services"></div>
                    <div class="spawn_services icon-group clickicon" title="Spawn all stored services"></div>
                    <div class="get_service_status icon-info-sign clickicon" title="Get status of this service"></div>
                    <div class="get_service_port icon-cog clickicon" title="Get remote listening port"></div>
                </article>
                <article class="filewatchers">
                    <header>File Watchers</header>
                    <div class="create_file_watch icon-plus clickicon" title="Create new file watch with action trigger"></div>
                    <div class="destroy_file_watch icon-minus clickicon" title="Destroy one of existing file watches"></div>
                </article>
                <article class="domains">
                    <header>Domains</header>
                    <div class="register_domain icon-asterisk clickicon" title="Registers given domain"></div>
                    <div class="registered_domains icon-bar-chart clickicon" title="Show registered domains"></div>
                </article>
                <article class="misc">
                    <header>Miscellaneous</header>
                    <div class="authorize icon-key clickicon" title="Authorize"></div>
                    <div class="mosh_session icon-sign-blank clickicon" title="Mosh terminal session"></div>
                    <div class="register_account icon-globe clickicon" title="Register this account with whole state and make it global"></div>
                    <div class="clone_igniter_for_user icon-qrcode clickicon" title="Create private software igniter"></div>
                    <div class="account_security_pass icon-user clickicon" title="Get account priviledges"></div>
                    <div class="restart_account_manager icon-warning-sign clickicon" title="Restart account manager"></div>
                    <div class="remove_all_reserved_ports icon-cogs clickicon" title="Remove all ports reserved by user"></div>
                    <div class="register_user_port icon-cog clickicon" title="Register user port"></div>
                    <div class="get_user_ports icon-dashboard clickicon" title="Get user ports"></div>
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
                    <section class="servicelist"></section>
                    <section class="admin"></section>
                    <section class="processlist"></section>
                </section>
            )

    }

}
