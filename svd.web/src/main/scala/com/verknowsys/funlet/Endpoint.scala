package com.verknowsys.funlet

import javax.servlet._
import javax.servlet.http._

import scala.collection.JavaConversions._
import com.verknowsys.served.utils.Logging
import java.io._
import scala.util.DynamicVariable

import org.fusesource.scalate.TemplateEngine
import org.fusesource.scalate.servlet.{ServletRenderContext, ServletTemplateEngine}

trait Endpoint extends PartialFunction[Request, Response] with Logging {
    def routes(implicit req: Request): PartialFunction[Request, Response]

    // PartialFunction implementation
    def apply(req: Request) = _routes(req)(req)
    def isDefinedAt(req: Request) = _routes(req).isDefinedAt(req)

    protected def _routes(implicit req: Request) = routes(req)

    // utils
    def render(name: String, attributes:(String, Any)*) = RenderTemplateResponse(name, attributes.toMap)

    def redirect(path: String) = RedirectResponse(path)

    def formParam(implicit req: Request) = req.params("form")
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
                    val request = new Request(rawRequest)
                    log.debug("Request: %s", request)
                    val response = apply(request)
                    log.debug("Response: %s", response)
                    response(servletResponse, this)
                } catch {
                    case e =>
                        log.error("Exception: %s", e)
                        renderErrorPage(e)
                        // StringResponse(500, renderErrorPage(e))(servletResponse, this)
                }
            }
        }
    }

    override def init(servletConfig: ServletConfig){
        log.debug("init")
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
