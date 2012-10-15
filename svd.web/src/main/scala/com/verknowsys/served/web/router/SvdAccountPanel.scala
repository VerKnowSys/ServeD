package com.verknowsys.served.web.router


import unfiltered.Cookie
import unfiltered.request._
import unfiltered.response._
import unfiltered.kit._
import unfiltered.filter._
import unfiltered.scalate.Scalate
import org.fusesource.scalate.TemplateEngine
import net.liftweb.json._
import java.util.UUID
import org.fusesource.scalate.{TemplateEngine, Binding}

import com.verknowsys.served._
import com.verknowsys.served.api._
import com.verknowsys.served.utils._
import com.verknowsys.served.web._
import com.verknowsys.served.web.router._
import com.verknowsys.served.db.{DBServer, DBClient, DB}

import unfiltered.jetty.Http
import java.net.URL
import unfiltered.filter.Plan
import net.liftweb.json._
import akka.actor._


class SvdAccountPanel(webManager: ActorRef, account: SvdAccount) extends Plan with Logging with SvdUtils {

    import QParams._
    import net.liftweb.json.JsonAST._
    import net.liftweb.json.JsonDSL._
    import webImplicits._
    import com.verknowsys.served.db._
    import com.verknowsys.served.web.merch._


    // def defineSomeRandomProducts = {
    //     val letter = new SpatialLetter(
    //         color = Color.Yellow,
    //         project = ProjectType.Predefined
    //         )
    //     val sticker = new Sticker(
    //         color = Color.Blue,
    //         project = ProjectType.UserDefined,
    //         stickerType = StickerType.Wall
    //         )
    //     val tshirt = new Tshirt(
    //         category = ClothingCategory.Polo,
    //         color = Color.Red,
    //         printSize = PrintSize.A5,
    //         printPlacement = TshirtPrintPlacement.Sleeve,
    //         project = ProjectType.UserDefined,
    //         size = Size.S
    //         )
    //     val letter2 = new SpatialLetter(
    //         color = Color.Red,
    //         project = ProjectType.UserDefined
    //         )
    //     val sticker2 = new Sticker(
    //         color = Color.Black,
    //         project = ProjectType.PredefinedVe,
    //         stickerType = StickerType.Standard
    //         )
    //     val tshirt2 = new Tshirt(
    //         category = ClothingCategory.Blouse,
    //         color = Color.Yellow,
    //         printSize = PrintSize.A6,
    //         printPlacement = TshirtPrintPlacement.Rear,
    //         project = ProjectType.Predefined,
    //         size = Size.XL
    //         )

    //     db << letter
    //     db << sticker
    //     db << tshirt
    //     db << tshirt2
    //     db << sticker2
    //     db << letter2
    //     log.info("Products defined")
    // }


    // def productBindings =
    //     Binding(name = "productClass", className = "String") ::
    //     Binding(name = "productCategory", className = "com.verknowsys.served.web.merch.ClothingCategory") ::
    //     Binding(name = "productColor", className = "com.verknowsys.served.web.merch.Color") ::
    //     Binding(name = "productPrintSize", className = "com.verknowsys.served.web.merch.PrintSize") ::
    //     Binding(name = "productPrintPlacement", className = "com.verknowsys.served.web.merch.TshirtPrintPlacement") ::
    //     Binding(name = "productProject", className = "com.verknowsys.served.web.merch.ProjectType") ::
    //     Binding(name = "productStickerType", className = "com.verknowsys.served.web.merch.StickerType") ::
    //     Binding(name = "productSize", className = "com.verknowsys.served.web.merch.Size") ::
    //     Binding(name = "productDescription", className = "String") ::
    //     Binding(name = "messages", className = "String") :: Nil


    // def productDefaultAttributes =
    //     ("productClass", "Clothing") ::
    //     ("productCategory", ClothingCategory.Tshirt) ::
    //     ("productColor", Color.White) ::
    //     ("productPrintSize", PrintSize.A4) ::
    //     ("productPrintPlacement", TshirtPrintPlacement.Front) ::
    //     ("productProject", ProjectType.PredefinedVector) ::
    //     ("productSize", Size.XXL) ::
    //     ("productStickerType", StickerType.Standard) ::
    //     ("productDescription", Dict("Product description")) ::
    //     ("messages", Dict("Enter new product information")) :: Nil


    def intent = {

        case req @ _ =>

            implicit val bindings: List[Binding] =
                Binding(name = "account", className = "com.verknowsys.served.api.SvdAccount") :: Nil
            implicit val additionalAttributes = ("account", account) :: Nil

            webManager ! Notify.Message("Siema")
            log.debug("GET /")
            Ok ~> Scalate(req, "/templates/index.jade")

        // case req @ GET(Path(Seg("productlist" :: Nil))) =>
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
