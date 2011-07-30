package com.verknowsys.funlet

import javax.servlet.http._

sealed class Param
final case class StringParam(value: String) extends Param
final case class MapParam(value: Map[String, Param]) extends Param
final case class ListParam(value: List[StringParam]) extends Param

object Params {
    type Raw = Map[String, List[String]]

    def encode(params: MapParam): Raw = {
        def combine(prefix: String, key: String) = if(prefix.isEmpty) key else prefix + "[" + key + "]"

        def parse(raw: Raw, key: String, param: Param): Raw = {
            param match {
                case StringParam(string) => raw + (key -> List(string))
                case ListParam(list) => raw + ((key+"[]") -> list.map(_.value))
                case MapParam(map) =>
                    raw ++ (map.map { case(k,v) => parse(raw, combine(key, k), v) } reduce { _ ++ _ })
            }
        }

        parse(Map[String, List[String]](), "", params)
    }
}

case class Request(
    method: HttpMethod,
    path: List[String] = Nil,
    host: String = "",
    port: Int = 80
    // params: MapParam = ,
    // headers: MapParam = Map(),
    // cookies: MapParam = Map(),
    // session: HttpSession
)
//
// class Request(sr: HttpServletRequest){
//     lazy val path = _path.split("/")
//
//     // def host = sr.getServerName
//
//     // def port = sr.getServerPort.toInt
//
//     // def isAjax =
//
//
//     protected
//
//     def _path = if(sr.getPathInfo != null) sr.getPathInfo else sr.getServletPath
//
// }

object Request {

}
