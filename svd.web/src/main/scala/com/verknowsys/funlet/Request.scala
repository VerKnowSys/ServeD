package com.verknowsys.funlet

import javax.servlet.http._

sealed trait Param
final case class StringParam(value: String) extends Param
final case class ListParam(value: List[StringParam]) extends Param
final case class MapParam(value: Map[String, Param]) extends Param {
    def merge(other: MapParam): MapParam = {
        val A = other
        val B = value

        MapParam((Map[String, Param]() /: (A.keys ++ B.keys)){
            case(acc, key) =>
                if(A contains key){
                    if(B contains key){
                        (A(key), B(key)) match {
                            case (a: MapParam, b: MapParam) => acc + (key -> (a merge b))
                            case _ => acc
                        }
                    } else {
                        acc + (key -> A(key))
                    }
                } else {
                    acc + (key -> B(key))
                }
        })
    }
}

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

    def decode(params: Raw): MapParam = {
        def normalize(param: Param): Param = param match {
            case MapParam(map) => MapParam(map.mapValues(normalize))
            case lp @ ListParam(list) => if(list.size == 1) list.head else lp
            case e => e
        }

        val Rx = "[^\\[\\]]+".r
        val empty = MapParam(Map())

        params.map {
            case(key, values) =>
                val param: Param = ListParam(values.map(StringParam(_)))
                (Rx.findAllIn(key) :\ param){ case(k, p) => MapParam(Map(k -> p)) }
        }.collect {
            case p: MapParam => p
        }.foldLeft(empty){
            case(m, p) => p merge m
        }.mapValues(normalize)
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
