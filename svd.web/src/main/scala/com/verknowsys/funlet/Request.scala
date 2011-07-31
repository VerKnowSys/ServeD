package com.verknowsys.funlet

import javax.servlet.http._
import scala.collection.JavaConversions._

class Request(servletRequest: HttpServletRequest){
    val method = _method

    val path = _path.split("/").toList.drop(1)

    def host = servletRequest.getServerName

    def port = servletRequest.getServerPort.toInt

    lazy val params = _params

    def headers(name: String) = Option(servletRequest.getHeader(name))

    def isAjax = headers("X-Requested-With").isDefined


    lazy val cookies = _cookies

    def session = servletRequest.getSession

    protected def _path =
        if(servletRequest.getPathInfo != null) servletRequest.getPathInfo
        else servletRequest.getServletPath

    protected def _method = HttpMethod(servletRequest.getMethod) match {
        case Head => Get
        case Post => Option(servletRequest.getParameter("_method")).map(m => HttpMethod(m.toUpperCase)) getOrElse Post
        case x => x
    }

    protected def _params = ParamsParser.decode(_servletParams.mapValues(_.toList))

    protected def _servletParams = servletRequest.getParameterMap.asInstanceOf[java.util.Map[String, Array[String]]].toMap

    protected def _cookies = Option(servletRequest.getCookies).getOrElse(Array()).toSeq.
                                groupBy(_.getName).mapValues(v => v map (_.getValue))

    override def toString = (method, path, _servletParams).toString
}

object Request {
    def unapply(request: Request) = Some(request.method, request.path)
}
