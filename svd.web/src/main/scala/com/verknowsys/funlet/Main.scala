package com.verknowsys.funlet

object ItemsDispatcher extends RestDispatcher("items", new ItemsEndpoint(_))
class ItemsEndpoint(request: Request) extends RestEndpoint(request){
    def index = StringResponse(200, "items#index")
    def `new` = StringResponse(200, "items#new")
    def create = StringResponse(200, "items#create")
    def show(id: String) = StringResponse(200, "items#show " + id)
    def edit(id: String) = StringResponse(200, "items#edit " + id)
    def update(id: String) = StringResponse(200, "items#update " + id)
    def destroy(id: String) = StringResponse(200, "items#destroy " + id)
}


class Main extends MainDispatcher {
    override def apply(request: Request) = request match {
        case Request(Get, Nil) => StringResponse(200, "Hello world")
        case req => performDispatch(req)
    }

    val dispatchers =
        ItemsDispatcher ::
        Nil
}

