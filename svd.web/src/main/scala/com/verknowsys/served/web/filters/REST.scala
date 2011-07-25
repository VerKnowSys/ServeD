package com.verknowsys.served.web.filters

import org.scalatra._
import scalate.ScalateSupport

abstract trait REST extends ScalatraFilter with ScalateSupport {
    def prefix: String


    def index: Map[String, Any]
    // def show
    // def new
    // def create
    // def edit
    // def update
    // def destroy

    beforeAll {
        contentType = "text/html"
    }

    get("/" + pathPrefix){
        render(pathPrefix + "/index", index)
    }

    protected lazy val pathPrefix = prefix

    protected def render(path: String, attributes: Map[String, Any] = Map()) =
        templateEngine.layout("/WEB-INF/scalate/templates/" + path + ".scaml", attributes)
}
