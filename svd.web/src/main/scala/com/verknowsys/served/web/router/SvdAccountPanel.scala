package com.verknowsys.served.web.router


import unfiltered.Cookie
import unfiltered.request._
import unfiltered.response._
import unfiltered.kit._
import unfiltered.filter._
// import unfiltered.scalate.Scalate
// import org.fusesource.scalate.TemplateEngine
import org.json4s._
import org.json4s.native._
import java.util.UUID
// import org.fusesource.scalate.{TemplateEngine, Binding}

import com.verknowsys.served._
import com.verknowsys.served.api._
import com.verknowsys.served.utils._
import com.verknowsys.served.web._
import com.verknowsys.served.web.router._
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
 *  Web API is used to perform communication with web manager
 */


class SvdAccountPanel(webManager: ActorRef, account: SvdAccount, webPort: Int) extends Plan with Logging with SvdUtils {

    import QParams._
    import org.json4s._
    import org.json4s.native._
    import org.json4s.JsonDSL._
    import webImplicits._
    import com.verknowsys.served.db._
    import com.verknowsys.served.web.merch._


    // implicit val bindings: List[Binding] =
            // Binding(name = "account", className = "com.verknowsys.served.api.SvdAccount") :: Nil

    // implicit val additionalAttributes = ("account", account) :: Nil
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
                block

            case _ =>
                log.debug("No svd auth cookie.")
                SvdWebAPI.apiRespond(webManager ? Error("Unauthorized. Use /Authorize/key first."))
        }
    }

    def intent = {


        /** API POST call #000  */
        case req @ POST(Path(Seg("Authorize" :: key :: Nil))) =>
            log.debug("POST on Authorize")
            if (key == "12345") // FIXME: XXX: TODO: hardcode auth key
                SetCookies(Cookie("svdauth", key)) ~>
                    ResponseString("""{"message": "Authorized successfully.", "status": 0}""")
            else
                Unauthorized ~>
                    ResponseString("""{"message": "Authorization failed.", "status": 5}""")

        case req @ POST(Path(Seg("Authorize" :: Nil)) & Params(params)) =>
            def param(key: String) = params.get(key).flatMap { _.headOption } getOrElse("")
            param("Authorize") match {
                case key: String =>
                    if (key == "12345") // FIXME: XXX: TODO: hardcode auth key
                        SetCookies(Cookie("svdauth", key)) ~>
                            ResponseString("""{"message": "Authorized successfully.", "status": 0}""")
                    else
                        Unauthorized ~>
                            ResponseString("""{"message": "Authorization failed.", "status": 5}""")
                case _ =>
                    Unauthorized ~>
                        ResponseString("""{"message": "Authorization failed.", "status": 5}""")
            }


        /** API POST call #001  */
        case req @ POST(Path(Seg("GetUserProcesses" :: Nil))) =>
            log.debug("POST on GetUserProcesses")
            SvdWebAPI.apiRespond(webManager ? System.GetUserProcesses(account.uid))


        /** API POST call #002  */
        case req @ POST(Path(Seg("RegisterDomain" :: domain :: Nil))) =>
            log.debug("POST /RegisterDomain by path")
            log.info("Given domain to be registered: %s", domain)
            SvdWebAPI.apiRespond(webManager ? System.RegisterDomain(domain, webManager))

        case req @ POST(Path(Seg("RegisterDomain" :: Nil)) & Params(params)) =>
            def param(key: String) = params.get(key).flatMap { _.headOption } getOrElse("")

            log.debug("POST /RegisterDomain from form params")
            param("RegisterDomain") match {
                case domain: String =>
                    log.debug("Given domain: %s", domain)
                    SvdWebAPI.apiRespond(webManager ? System.RegisterDomain(domain, webManager))

                case _ =>
                    JsonContent ~> ResponseString("{\"message\": \"Invalid API request.\", \"status\":3}")
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



        /** API POST call #DEFAULT  */
        case req @ POST(_) =>
            JsonContent ~> ResponseString("{\"message\": \"Invalid API request.\", \"status\":3}")


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

        /** API GET call #DEFAULT  */
        case req @ _ =>
            log.debug("GET /")
            Ok ~> layout(
                <section class="header"></section>
                <section class="content">
                    <div class="target">Coś sensownego</div>
                    <div class="target2">Cel</div>
                    <section class="pslist"></section>
                </section>
            )

        //     log.debug("GET /productlist")

        //     val clothings = Clothings(db)
        //     val letters = SpatialLetters(db)
        //     val stickers = Stickers(db)


        //         Binding(name = "letters", className = "List[com.verknowsys.served.web.merch.SpatialLetter]") ::
        //         Binding(name = "stickers", className = "List[com.verknowsys.served.web.merch.Sticker]") :: Nil
        //     implicit val additionalAttributes =
        //         ("clothings", clothings.toList) ::
        //         ("letters", letters.toList) ::
        //         ("stickers", stickers.toList) :: Nil
        //     log.warn("Current resource: %s", System.getProperty("user.dir"))
        //     Ok ~> Scalate(req, "/templates/productlist.jade")



        // // adding new product
        // case req @ GET(Path(Seg("product" :: Nil))) =>
        //     log.debug("GET /product")
        //     implicit val bindings = productBindings
        //     implicit val additionalAttributes = productDefaultAttributes
        //     Ok ~> Scalate(req, "/templates/product.jade")


        // case req @ POST(Path(Seg("product" :: Nil)) & Params(params)) =>
        //     log.debug("POST /product with params %s".format(params.mkString(", ")))

        //     def param(k: String) = params.get(k).flatMap { _.headOption } getOrElse("")

        //     implicit val bindings = productBindings
        //     val expected = for {

        //         description <- lookup("product_description") is
        //             trimmed is
        //             nonempty(Dict("Product description cannot be empty"))

        //     } yield {
        //         // creating new product
        //         log.info("Creating new product based on params: %s".format(params.mkString(", ")))
        //         param("product_class") match {
        //             case "Clothing" =>
        //                 db << Clothing(
        //                     category = ClothingCategory(param("product_category")),
        //                     color = Color(param("product_color")),
        //                     printSize = PrintSize(param("product_print_size")),
        //                     printPlacement = TshirtPrintPlacement(param("product_print_placement")),
        //                     project = ProjectType(param("product_project")),
        //                     size = Size(param("product_size")),
        //                     description = param("product_description")
        //                     )

        //             case "SpatialLetter" =>
        //                 db << SpatialLetter(
        //                     color = Color(param("product_color")),
        //                     project = ProjectType(param("product_project")),
        //                     description = param("product_description")
        //                     )

        //             case "Sticker" =>
        //                 db << Sticker(
        //                     color = Color(param("product_color")),
        //                     project = ProjectType(param("product_project")),
        //                     stickerType = StickerType(param("product_sticker_type")),
        //                     description = param("product_description")
        //                     )

        //             case any =>
        //                 log.error("Couldn't create object for product category: %s".format(any))
        //         }

        //         Ok ~> Redirect("/productlist")
        //     }


        //     expected(params) orFail {
        //         fails =>
        //             val failures =
        //                 fails.map {
        //                     fail =>
        //                         log.error("Validation error: %s".format(fail.error))
        //                         fail.error
        //                 }

        //             implicit val additionalAttributes =
        //                 ("productClass", param("product_class")) ::
        //                 ("productCategory", ClothingCategory(param("product_category"))) ::
        //                 ("productColor", Color(param("product_color"))) ::
        //                 ("productPrintSize", PrintSize(param("product_print_size"))) ::
        //                 ("productPrintPlacement", TshirtPrintPlacement(param("product_print_placement"))) ::
        //                 ("productProject", ProjectType(param("product_project"))) ::
        //                 ("productSize", Size(param("product_size"))) ::
        //                 ("productStickerType", StickerType(param("product_sticker_type"))) ::
        //                 ("productDescription", param("product_description")) ::
        //                 ("messages", failures.mkString(", ")) :: Nil

        //             Ok ~> Scalate(req, "/templates/product.jade")
        //     }






        // case POST(Path(Seg(
        //     "generatesomeproducts" :: Nil))) =>
        //         defineSomeRandomProducts
        //         JsonContent ~> ResponseString(
        //             message("Products defined."))


        // case POST(Path(Seg(
        //     "getfulllistofproducts" :: Nil))) =>
        //         JsonContent ~> ResponseString(
        //             message("Products defined: %s".format(gatherAllProductsAvailable.mkString(", "))))

    }


}
