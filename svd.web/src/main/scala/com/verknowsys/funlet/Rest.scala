package com.verknowsys.funlet

trait Dispatcher extends PartialEndpoint {
    type Dispatch = PartialFunction[Request, Endpoint]
    def dispatch: Dispatch

    def apply(request: Request) = performDispatch(request)

    def isDefinedAt(request: Request) = _dispatch.isDefinedAt(request)

    def performDispatch(request: Request) = _dispatch(request)(request)

    lazy val _dispatch = dispatch
}

abstract trait MainDispatcher extends MainEndpoint {
    val dispatchers: List[PartialEndpoint]

    def apply(request: Request) = performDispatch(request)

    def performDispatch(request: Request) = (dispatchers :+ default reduce (_ orElse _))(request)

    val default: PartialEndpoint = {
        case _ => StringResponse(404, "NotFound")
    }
}

abstract class RestDispatcher(prefix: String, endpoint: Request => RestEndpoint) extends Dispatcher {
    val Prefix = prefix
    def dispatch = {
        case Request(Get, Prefix :: Nil)                    => endpoint(_).index
        case Request(Get, Prefix :: "new" :: Nil)           => endpoint(_).`new`
        case Request(Post, Prefix :: Nil)                   => endpoint(_).create
        case Request(Get, Prefix :: id :: Nil)              => endpoint(_).show(id)
        case Request(Get, Prefix :: id :: "edit" :: Nil)    => endpoint(_).edit(id)
        case Request(Put, Prefix :: id :: Nil)              => endpoint(_).update(id)
        case Request(Delete, Prefix :: id :: Nil)           => endpoint(_).destroy(id)
    }
}

abstract class RestEndpoint(request: Request) {
    def index: Response
    def `new`: Response
    def show(id: String): Response
    def edit(id: String): Response
    def create: Response
    def update(id: String): Response
    def destroy(id: String): Response
}
