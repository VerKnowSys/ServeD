package com.verknowsys

import scala.xml.NodeSeq

package object funlet {
    type Session = Map[String, Any]
    type Headers = Map[String, String]
    type Cookies = Map[String, String]

    // params
    implicit def String2StringParam(s: String): StringParam = StringParam(s)
    implicit def StringParam2String(sp: StringParam) = sp.value
    implicit def List2ListParam(list: List[StringParam]): ListParam = ListParam(list)
    implicit def ListParam2List(lp: ListParam) = lp.value
    implicit def Map2MapParam(map: Map[String, Param]): MapParam = MapParam(map)
    implicit def MapParam2Map(mp: MapParam) = mp.value

    implicit def StringParam2MapParam(sp: StringParam) = Empty
    implicit def ListParam2MapParam(lp: ListParam) = Empty
    val Empty = MapParam(Map())

    // response
    implicit def NodeSeq2StringResponse(node: NodeSeq) = new StringResponse(200, node.toString)
    implicit def String2StringResponse(str: String) = new StringResponse(200, str)
    implicit def OptionResponse2Response(opt: Option[Response]) = opt getOrElse NotFoundResponse

    // forms
    implicit def fieldToOption[E,T](field: Field[E, T]) = field.value
    type Validator[T] = Function1[T, Option[String]]

    // misc
    implicit def Pair2Map(pair: (String, Any)) = Map(pair)
}
