package com.verknowsys.served.web.endpoints

import org.scalatra.ScalatraFilter
import scala.collection.mutable.ListBuffer

class Main extends Endpoint with LoggerEndpoint
                            with GitEndpoint
