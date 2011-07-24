package com.verknowsys.served.web

import com.verknowsys.served.api.ApiMessage

package object lib {
    implicit def ApiMessage2Requst(msg: ApiMessage) = new {
        def <>[T](pf: PartialFunction[Any, T]) = Session.api.request(msg)(pf)
    }
}
