package com.verknowsys.served.web.endpoints

import com.verknowsys.funlet._

class Main extends MainEndpoint {
    override val endpoints = GitEndpoint :: LoggerEndpoint :: Nil

    override def routes(implicit req: Request) = {
        case Get(path) => render("index")
    }

}
