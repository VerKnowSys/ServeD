package com.verknowsys.funlet

import javax.servlet.http._

trait Response extends Function1[HttpServletResponse, Unit]

case class RedirectResponse(path: String) extends Response {
    def apply(servletResponse: HttpServletResponse){
        servletResponse.sendRedirect(path)
    }
}

case class StringResponse(
    status: Int,
    body: String,
    headers: Map[String, String] = Map(),
    cookies: Map[String, String] = Map()
) extends Response {
    def apply(servletResponse: HttpServletResponse){
        servletResponse.setStatus(status)
        servletResponse.getWriter.print(body)
        headers foreach { case(name, value) => servletResponse.setHeader(name, value) }
        cookies foreach { case(name, value) => servletResponse.addCookie(new Cookie(name, value)) }
    }
}
