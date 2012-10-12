package com.verknowsys.served.web.router


import unfiltered.Cookie
import unfiltered.request._
import unfiltered.response._
import unfiltered.kit._
import unfiltered.filter._
import net.liftweb.json._
import java.util.UUID
// import scalaz._

import com.verknowsys.served._
import com.verknowsys.served.utils._
import com.verknowsys.served.web._


class Products extends Plan with Database with Logging with SvdUtils {

    import QParams._
    import net.liftweb.json.JsonAST._
    import net.liftweb.json.JsonDSL._
    // import net.liftweb.json.Extraction._
    // import net.liftweb.json.Printer._
    import webImplicits._
    import com.verknowsys.served.db._
    import com.verknowsys.served.web.merch._
    // import Scalaz._


    // def caseClassFields(cc: scala.Product) = {
    //     val pc = new ProductCompletion(cc)
    //     pc.caseNames.zip(pc.caseFields.map{a => a.toString}) //.toMap
    // }


    def gatherObjects(productType: String) =
        (productType match {
            case "SpatialLetter" => SpatialLetters(db)
            case "Sticker" => Stickers(db)
            case "Clothing" => Clothings(db)
            case _ => Clothings(db)
        })


    def intent = {

        case GET(Path(Seg(
            "search" :: productType :: Nil)) & Params(conditions)) =>

                def condition(k: String) = conditions.get(k).flatMap { _.headOption } getOrElse("")

                def checkConditions(objects: Map[String, String]): Boolean = {
                    val keys = objects map { case (key, value) => key}
                    val conditionsMap = (for (key <- keys) yield (key -> condition(key))).filterNot { a => a._2.isEmpty() }

                    objects.filterKeys {
                        key =>
                            conditionsMap.exists(_._1 == key)
                    } map {
                        case (key, value) =>
                            if (conditionsMap.toList contains (key -> value)) {
                                log.trace("FOUND match %s  VS  (%s,%s)".format((key -> value), key, value))
                            } else {
                                log.trace("NO MATCH for: %s  VS  (%s,%s)".format((key -> value), key, value))
                                return false
                            }
                    }
                    true
                }

                val results = (gatherObjects(productType).filter {
                    case record =>
                        checkConditions(caseClassFields(record))
                }) map {
                    caseClassFields _
                }
                val json = compact(render(results))
                JsonContent ~> ResponseString(json)

    }


}
