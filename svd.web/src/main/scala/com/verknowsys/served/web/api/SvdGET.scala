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
 *  Web GET API is used to perform communication with web manager
 */
class SvdGET(webManager: ActorRef, account: SvdAccount, webPort: Int) extends SvdWebAPI(webManager) {

    import QParams._
    import org.json4s._
    import org.json4s.native._
    import org.json4s.JsonDSL._
    import webImplicits._


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
