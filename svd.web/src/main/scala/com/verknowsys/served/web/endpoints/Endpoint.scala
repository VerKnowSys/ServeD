package com.verknowsys.served.web.endpoints

import org.scalatra._
import scalate.ScalateSupport

import com.verknowsys.forms._
import com.verknowsys.served.utils.Logging

abstract trait Endpoint extends ScalatraFilter with ScalateSupport with FlashMapSupport with MethodOverride with Logging {
    def publicAfterFilters = afterFilters.toList

    beforeAll {
        contentType = "text/html"
    }

    protected override def effectiveMethod: HttpMethod = {
        HttpMethod(request.getMethod) match {
            case Post => params.get("_method").map(m => HttpMethod(m.toUpperCase)) getOrElse Post
            case Head => Get
            case x => x
        }
    }

    def formParams = _parseParams(params, "form")

    val NestedParamsRegex = "^(.+)\\[(.+)\\]$".r

    def _parseParams(map: Map[String, String], prefix: String) =
        (Map[String, String]() /: map){
            case (xs, (NestedParamsRegex(p, k), v)) if p == prefix => xs + (k -> v)
            case (xs, (k,v)) => xs
        }

    def render(path: String, attributes: (String, Any)*): Unit = render(path, attributes.toMap)
    def render(path: String, attributes: Map[String, Any] = Map()) =
        templateEngine.layout("/WEB-INF/scalate/templates/" + path + ".scaml", attributes + ("flash" -> flash))
}
