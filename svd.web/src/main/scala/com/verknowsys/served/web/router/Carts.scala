package com.verknowsys.served.web.router


import unfiltered.Cookie
import unfiltered.request._
import unfiltered.response._
import unfiltered.kit._
import unfiltered.filter._
import net.liftweb.json._
import java.util.UUID

import com.verknowsys.served._
import com.verknowsys.served.api._
import com.verknowsys.served.utils._
import com.verknowsys.served.web._


class Carts extends Plan with Database with Logging with SvdUtils {

    import QParams._
    import net.liftweb.json.JsonAST._
    import net.liftweb.json.JsonDSL._
    import webImplicits._
    import com.verknowsys.served.db._
    import com.verknowsys.served.web.merch._


    def intent = {

        case POST(Path(Seg(
            "addtocart" :: product :: Nil)) & Cookies(cookies)) =>

                cookies("cart") match {

                    case Some(Cookie("cart", cart, _, _, _, _, _, _)) =>
                        val merged = parse("""%s""".format(cart)) merge parse("""["%s"]""".format(product))
                        JsonContent ~>
                            SetCookies(Cookie("cart", compact(render(merged)))) ~>
                                ResponseString("""{"message":"ok","details":"merged cart","data":%s}""".format(compact(render(merged))))

                    case None =>
                        val json = List("%s".format(product))
                        JsonContent ~>
                            SetCookies(Cookie("cart", compact(render(json)))) ~>
                                ResponseString("""{"message":"ok","details":"new cart","data":%s}""".format(compact(render(json))))

                }

    }


}
