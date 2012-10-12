package com.verknowsys.served.web


import unfiltered.Cookie
import unfiltered.request._
import unfiltered.response._
import unfiltered.kit._
import unfiltered.jetty.Http
import java.net.URL
import unfiltered.filter.Plan
import net.liftweb.json._


import com.verknowsys.served._
import com.verknowsys.served.utils._
import com.verknowsys.served.web.router._

// /** unfiltered plan */
// class Default extends Plan {
//     import QParams._
//     import net.liftweb.json.JsonAST._
//     import net.liftweb.json.JsonDSL._


//     val logger = Logger(classOf[App])


//     def intent = { // GZip
//         case GET(Path("/cookie")) =>
//             val jsonList = List("42fregvsdgvfd", "j7ytdhfdghfdgh")
//             val value = compact(render(jsonList))
//             logger.info("Value: %s".format(value))
//             SetCookies(Cookie("cart", value))


//         case GET(Path(Seg(Nil))) =>
//             logger.debug("GET /")
//             Ok ~> view(Map.empty)(<p> What say you? </p>)


//         case POST(Path(Seg("subm" :: Nil)) & Params(params)) =>
//             logger.debug("POST /subm")
//             val vw = view(params)_
//             val expected = for {
//                 int <- lookup("int") is
//                     int { _ + " is not an integer" } is
//                     required("missing int")

//                 word <- lookup("palindrome") is
//                     trimmed is
//                     nonempty("Palindrome is empty") is
//                     pred(palindrome, { _ + " is not a palindrome" }) is
//                     required("missing palindrome")
//             } yield vw(<p>Yup. { int.get } is an integer and { word.get } is a palindrome. </p>)

//             expected(params) orFail {
//                 fails =>
//                     vw(<ul> { fails.map { f => <li>{f.error} </li> } } </ul>)
//             }


//         case _ =>
//             MethodNotAllowed ~> ResponseString("Method not allowed")

//     }


//     def palindrome(s: String) = s.toLowerCase.reverse == s.toLowerCase


//     def view(params: Map[String, Seq[String]])(body: scala.xml.NodeSeq) = {
//         def p(k: String) = params.get(k).flatMap { _.headOption } getOrElse("")
//         Html(
//             <html>
//                 <head>
//                     <title>uf example</title>
//                     <link rel="stylesheet" type="text/css" href="/assets/css/app.css"/>
//                 </head>
//                 <body>
//                     <script type="text/javascript" src="/assets/js/jquery-1.8.0.min.js"/>
//                     <div id="container">
//                         { body }
//                         <form method="POST" action="/subm">
//                             <div>Integer
//                                 <input type="text" name="int" value={ p("int") } />
//                             </div>
//                             <div>Palindrome
//                                 <input type="text" name="palindrome" value={ p("palindrome") } />
//                             </div>
//                             <input type="submit" />
//                         </form>
//                     </div>
//                 </body>
//             </html>
//         )
//     }
// }

/** embedded server */
object Server extends Logging with SvdUtils {

    import webImplicits._

    def spawnServer(port: Int) = {
        val base = new URL(getClass.getResource("/public/"), ".")
        val http = Http(port)

        val admins = new Admins
        val products = new Products
        val orders = new Orders
        val carts = new Carts

        http
            .context("/assets") {
                _.resources(base)
            }
            .filter(admins)
            .filter(products)
            .filter(orders)
            .filter(carts)
            .run({
                svr =>
                    // unfiltered.util.Browser.open(http.url) // TODO: if development
            }, {
                svr =>
                    admins.terminate
                    orders.terminate
                    products.terminate
                    carts.terminate
                    DatabaseServer.server.close
                    log.info("Shutting down web console")
            })
    }


    /* svd launcher */
    def apply(port: Int = 51234) = {
        spawnServer(port)
    }


    /* FALLBACK / LEGACY SPAWN */
    def main(args: Array[String]) {
        spawnServer(51234)
    }


}
