package com.verknowsys.served.web

import org.scalatra._
import scalate.ScalateSupport

trait SvdFilter extends ScalatraFilter with ScalateSupport {
    beforeAll {
        contentType = "text/html"
    }

    protected def render(path: String, attributes: Map[String, Any] = Map()) =
        templateEngine.layout("/WEB-INF/scalate/templates/" + path + ".scaml", attributes)

}

class Main extends SvdFilter {
    get("/") {
        render("hello")
    }
}
