/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.api


import com.verknowsys.served._
import com.verknowsys.served.utils._
import java.util.concurrent.TimeoutException
import unfiltered.response._
import scala.concurrent._
import scala.concurrent.duration._
import akka.util.Timeout


/**
 *  This object is a container of functions used in Web API
 *
 *  @author dmilith
 */
object SvdWebAPI extends SvdUtils {


    def apiRespond[T](future: Future[T]) = {
        try {
            implicit val timeout = Timeout(SvdConfig.defaultAPITimeout / 1000 seconds)
            Await.result(future, timeout.duration) match {
                case ApiSuccess(x) =>
                    JsonContent ~> ResponseString(s"""{"message": "${x}", "status":0}""")

                case jsonContent: String =>
                    log.trace(s"Sending WebAPI JSON content: ${jsonContent}")
                    JsonContent ~> ResponseString(s"${jsonContent}")

                case ApiError(x) =>
                    JsonContent ~> ResponseString(s"""{"message": "${x}", "status":2}""")
            }
        } catch {
            case e: TimeoutException =>
                log.warn("Timout occured while processing API Request.")
                JsonContent ~> ResponseString("{\"message\": \"API request timed out.\", \"status\":3}")

            case x: Throwable =>
                log.error("API Request caused exception: %s.".format(x))
                JsonContent ~> ResponseString("{\"message\": \"API request crashed: %s.\", \"status\":4}".format(x))
        }
    }


}
