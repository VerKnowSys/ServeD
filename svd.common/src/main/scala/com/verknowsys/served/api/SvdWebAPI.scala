package com.verknowsys.served.api


import com.verknowsys.served._
import com.verknowsys.served.utils._

import unfiltered.request._
import unfiltered.response._
import java.net.URL
import unfiltered.filter.Plan
import org.json4s._
import org.json4s.native._
import akka.actor._
import akka.dispatch._
import akka.util.Timeout
import akka.util.duration._


/**
 *  @author dmilith
 *      This object is a container of functions used in Web API
 *
 */
object SvdWebAPI extends SvdUtils {


    def apiRespond[T](future: Future[T]) = {
        implicit val timeout = Timeout(SvdConfig.defaultAPITimeout / 1000)
        try {
            Await.result(future, timeout.duration) match {
                case Success =>
                    JsonContent ~> ResponseString("{\"message\": \"API request completed successfully.\"}")

                case x =>
                    JsonContent ~> ResponseString("{\"message\": \"Error occured while processing API request: %s\"}".format(x))
            }
        } catch {
            case e: java.util.concurrent.TimeoutException =>
                log.warn("Timout occured while processing API Request.")
                JsonContent ~> ResponseString("{\"message\": \"API request timed out.\"}")

            case x: Throwable =>
                log.error("API Request caused exception: %s.".format(x))
                JsonContent ~> ResponseString("{\"message\": \"API request crashed: %s.\"}".format(x))
        }
    }


}