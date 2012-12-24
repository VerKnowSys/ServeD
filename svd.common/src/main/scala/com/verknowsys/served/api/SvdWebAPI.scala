package com.verknowsys.served.api


import com.verknowsys.served._
import com.verknowsys.served.utils._

import unfiltered.response._
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
        try {
            implicit val timeout = Timeout(SvdConfig.defaultAPITimeout / 1000 seconds)
            Await.result(future, timeout.duration) match {
                case Success =>
                    JsonContent ~> ResponseString("{\"message\": \"API request completed successfully.\", \"status\":0}")

                case jsonContent: String =>
                    log.trace("Passing json content: %s".format(jsonContent))
                    JsonContent ~> ResponseString("%s".format(jsonContent))

                case Error(x) =>
                    JsonContent ~> ResponseString("{\"message\": \"Error occured while processing API request: %s\", \"status\":2}".format(x))
            }
        } catch {
            case e: java.util.concurrent.TimeoutException =>
                log.warn("Timout occured while processing API Request.")
                JsonContent ~> ResponseString("{\"message\": \"API request timed out.\", \"status\":3}")

            case x: Throwable =>
                log.error("API Request caused exception: %s.".format(x))
                JsonContent ~> ResponseString("{\"message\": \"API request crashed: %s.\", \"status\":4}".format(x))
        }
    }


}
