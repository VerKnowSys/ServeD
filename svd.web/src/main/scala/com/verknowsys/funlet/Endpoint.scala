package com.verknowsys.funlet

import javax.servlet._
import javax.servlet.http._

import scala.collection.JavaConversions._

trait MainEndpoint extends HttpServlet with Endpoint {
    override def service(servletRequest: HttpServletRequest, servletResponse: HttpServletResponse){
        apply(liftRequest(servletRequest))(servletResponse)
    }

    protected def liftRequest(servletRequest: HttpServletRequest): Request = new Request(servletRequest)
}
