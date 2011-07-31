package com.verknowsys.funlet

// Scalatra mapping
abstract class HttpMethod(val name: String){
    def unapply(req: Request) = if(req.method == this) Some(req.path) else None
}
object HttpMethod {
    def apply(name: String) = name.toUpperCase match {
        case "OPTIONS"   => Options
        case "GET"       => Get
        case "HEAD"      => Head
        case "POST"      => Post
        case "PUT"       => Put
        case "DELETE"    => Delete
        case "TRACE"     => Trace
        case "CONNECT"   => Connect
    }
}

object Options extends HttpMethod("OPTIONS")
object Get extends HttpMethod("GET")
object Head extends HttpMethod("HEAD")
object Post extends HttpMethod("POST")
object Put extends HttpMethod("PUT")
object Delete extends HttpMethod("DELETE")
object Trace extends HttpMethod("TRACE")
object Connect extends HttpMethod("CONNECT")
