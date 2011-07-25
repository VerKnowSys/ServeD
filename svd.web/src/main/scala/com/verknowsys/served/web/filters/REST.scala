package com.verknowsys.served.web.filters

import org.scalatra._
import scalate.ScalateSupport

import com.verknowsys.forms._
import com.verknowsys.served.utils.Logging

abstract trait REST extends ScalatraFilter with ScalateSupport with Logging {
    def prefix: String


    def index: Map[String, Any]
    // def show
    // def new
    def create
    // def edit
    // def update
    // def destroy

    // Path helpers
    def indexPath = pathPrefix
    def createPath = pathPrefix

    beforeAll {
        contentType = "text/html"
    }

    get(pathPrefix){
        render("index", index)
    }

    post(pathPrefix){
        create
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
