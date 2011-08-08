package com.verknowsys.funlet

import javax.servlet.http._

abstract class Response(
    val headers: Headers = Map(),
    val cookies: Cookies = Map(),
    val session: Session = Map()
){
    def apply(servletRequest: HttpServletRequest, servletResponse: HttpServletResponse, main: MainEndpoint){
        headersWithContentType foreach { case(name, value) => servletResponse.setHeader(name, value) }
        cookies foreach { case(name, value) => servletResponse.addCookie(new Cookie(name, value)) }

        lazy val servletSession = servletRequest.getSession(true)
        session foreach { case(name, value) => servletSession.setAttribute(name, value) }
    }

    protected def headersWithContentType =
        if(headers.contains("Content-Type")) headers
        else headers + ("Content-Type" -> "text/html")
}

class RedirectResponse(
    path: String,
    headers: Headers = Map(),
    cookies: Cookies = Map(),
    session: Session = Map()
) extends Response(headers, cookies, session) {
    override def apply(servletRequest: HttpServletRequest, servletResponse: HttpServletResponse, main: MainEndpoint){
        super.apply(servletRequest, servletResponse, main)
        servletResponse.sendRedirect(path)
    }

    override def toString = "Redirect: " + path
}

class StringResponse(
    status: Int,
    body: String,
    headers: Headers = Map(),
    cookies: Cookies = Map(),
    session: Session = Map()
) extends Response(headers, cookies, session) {
    override def apply(servletRequest: HttpServletRequest, servletResponse: HttpServletResponse, main: MainEndpoint){
        super.apply(servletRequest, servletResponse, main)
        servletResponse.setStatus(status)
        servletResponse.getWriter.print(body)
    }

    override def toString = (status, body, headers, cookies).toString
}

class RenderTemplateResponse(
    name: String,
    attributes: Map[String, Any],
    headers: Headers = Map(),
    cookies: Cookies = Map(),
    session: Session = Map()
) extends Response(headers, cookies, session) {
    override def apply(servletRequest: HttpServletRequest, servletResponse: HttpServletResponse, main: MainEndpoint){
        super.apply(servletRequest, servletResponse, main)
        main.templateEngine.layout("/WEB-INF/scalate/templates/" + name + ".jade", attributes)
    }

    override def toString = "Render tempate: " + name
}

object NotFoundResponse extends StringResponse(404, "NotFound")
