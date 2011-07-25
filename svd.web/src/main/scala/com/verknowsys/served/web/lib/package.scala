package com.verknowsys.served.web

import com.verknowsys.served.api.ApiMessage

package object lib {
    implicit def list2itemsMap[T](list: List[T]) = Map("items" -> list)
    // implicit def list2itemsMap[T](list: List[T]) = Map("items" -> list)

    implicit def ApiMessage2Requst(msg: ApiMessage) = new {
        def <>[T](pf: PartialFunction[Any, T]) = API.request(msg)(pf)
    }
}
