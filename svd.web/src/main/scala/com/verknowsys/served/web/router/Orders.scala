package com.verknowsys.served.web.router


import unfiltered.Cookie
import unfiltered.request._
import unfiltered.response._
import unfiltered.kit._
import unfiltered.filter._
import net.liftweb.json._
import java.util.UUID

import com.verknowsys.served._
import com.verknowsys.served.utils._
import com.verknowsys.served.web._


class Orders extends Plan with Database with Logging with SvdUtils {

    import QParams._
    import net.liftweb.json.JsonAST._
    import net.liftweb.json.JsonDSL._
    import webImplicits._
    import com.verknowsys.served.db._
    import com.verknowsys.served.web.merch._


    def intent = {

        case POST(Path(Seg(
            "order" :: cartUUID :: userEmail :: Nil)) & Cookies(cookies)) =>
                cookies("cart") match {
                    case Some(Cookie(_, cart, _, _, _, _, _, _)) =>
                        val cartJSON = ("cartUUID" -> cartUUID) ~ ("contains" -> cart)
                        val json = cartJSON ~ ("email" -> userEmail) ~ ("confirmed" -> false) ~ ("orderUUID" -> "%s".format(UUID.randomUUID()))
                        // Mail("Wiadomość mailowa", "dmilith@verknowsys.com")
                        JsonContent ~> ResponseString(json)

                    case _ =>
                        JsonContent ~> ResponseString(message("Cart contains no products."))
                }

    }


}
