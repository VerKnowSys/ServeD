package com.verknowsys.served.web.filters

import org.scalatra._
import scalate.ScalateSupport

import com.verknowsys.forms._
import com.verknowsys.served.utils.Logging

abstract trait Action extends ScalatraFilter with ScalateSupport with Logging {
    def prefix: String

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

    protected lazy val pathPrefix = "/" + prefix

    protected def render(path: String, attributes: Map[String, Any] = Map()) =
        templateEngine.layout("/WEB-INF/scalate/templates/" + pathPrefix + "/" + path + ".scaml", attributes)

}

abstract trait IndexAction extends Action {
    def index: Map[String, Any]

    def indexPath = pathPrefix

    get(pathPrefix){
        render("index", index)
    }
}

abstract trait CreateAction extends Action {
    def create

    def createPath = pathPrefix

    post(pathPrefix){
        create
    }
}

abstract class REST(val prefix: String = "") extends IndexAction
                                            with CreateAction

