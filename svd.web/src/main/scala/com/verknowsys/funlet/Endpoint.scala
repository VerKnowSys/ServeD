package com.verknowsys.funlet

import java.io._
import javax.servlet._
import javax.servlet.http._
import scala.collection.JavaConversions._
import scala.util.DynamicVariable

import org.fusesource.scalate.TemplateEngine
import org.fusesource.scalate.servlet.{ServletRenderContext, ServletTemplateEngine}

trait Endpoint extends PartialFunction[Request, Response] {
    def routes(implicit req: Request): PartialFunction[Request, Response]

    // PartialFunction implementation
    def apply(req: Request) = _routes(req)(req)
    def isDefinedAt(req: Request) = _routes(req).isDefinedAt(req)

    protected def _routes(implicit req: Request) = routes(req)

    // utils

    def defaultTemplateExtension = ".jade"

    def render(name: String, attributes:(String, Any)*) = new RenderTemplateResponse(name + defaultTemplateExtension, attributes.toMap)

    def redirect(path: String, flash: Map[String, Any] = Map(), session: Map[String, Any] = Map()) = new RedirectResponse(path, session = session)

    def formParam(implicit req: Request) = req.params("form")

    def session(name: String)(implicit req: Request) = req.session(name)
}

trait MainEndpoint extends HttpServlet with Endpoint {
    // routing
    val endpoints: List[PartialFunction[Request, Response]] = Nil

    override def _routes(implicit req: Request) = (endpoints :+ routes) reduce (_ orElse _)

    // low-level stuff
    protected val _rawRequest = new DynamicVariable[HttpServletRequest](null)
    protected val _rawResponse = new DynamicVariable[HttpServletResponse](null)
    protected var _config: ServletConfig = _
    protected var _templateEngine: TemplateEngine = _

    def rawRequest = _rawRequest.value
    def rawResponse = _rawResponse.value
    def config = _config
    def templateEngine = _templateEngine

    protected trait TplEngine {
        self: TemplateEngine =>

        override def createRenderContext(uri: String, out: PrintWriter) = MainEndpoint.this.createRenderContext
        override def isDevelopmentMode = true
    }

    def createRenderContext = new ServletRenderContext(templateEngine, rawRequest, rawResponse, servletContext)


    override def service(servletRequest: HttpServletRequest, servletResponse: HttpServletResponse){
        _rawRequest.withValue(servletRequest){
            _rawResponse.withValue(servletResponse){
                try {
                    apply(new Request(rawRequest))(rawRequest, rawResponse, this)
                } catch {
                    case e => renderErrorPage(e)
                }
            }
        }
    }

    override def init(servletConfig: ServletConfig){
        _config = servletConfig
        _templateEngine = new ServletTemplateEngine(config) with TplEngine
    }

    protected def servletContext = config.getServletContext

    protected def renderErrorPage(e: Throwable) = {
        val renderContext = createRenderContext
        renderContext.setAttribute("javax.servlet.error.exception", Some(e))
        templateEngine.layout("/WEB-INF/scalate/errors/500.scaml", renderContext)
    }
}
