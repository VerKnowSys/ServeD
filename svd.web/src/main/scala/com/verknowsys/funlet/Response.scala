package com.verknowsys.funlet

import javax.servlet.http._

trait Response extends Function2[HttpServletResponse, MainEndpoint, Unit]

case class RedirectResponse(path: String) extends Response {
    def apply(servletResponse: HttpServletResponse, main: MainEndpoint){
        servletResponse.sendRedirect(path)
    }

    override def toString = "Redirect: " + path
}

case class StringResponse(
    status: Int,
    body: String,
    headers: Map[String, String] = Map("Content-Type" -> "text/html"),
    cookies: Map[String, String] = Map()
) extends Response {
    def apply(servletResponse: HttpServletResponse, main: MainEndpoint){
        servletResponse.setStatus(status)
        servletResponse.getWriter.print(body)
        headers foreach { case(name, value) => servletResponse.setHeader(name, value) }
        cookies foreach { case(name, value) => servletResponse.addCookie(new Cookie(name, value)) }
    }

    override def toString = (status, body, headers, cookies).toString
}

case class RenderTemplateResponse(
    name: String,
    attributes: Map[String, Any]
) extends Response {
    def apply(servletResponse: HttpServletResponse, main: MainEndpoint){
        main.templateEngine.layout("/WEB-INF/scalate/templates/" + name + ".scaml", attributes)
    }

    override def toString = "Render tempate: " + name
}
