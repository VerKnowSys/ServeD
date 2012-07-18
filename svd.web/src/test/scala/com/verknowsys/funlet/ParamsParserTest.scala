package com.verknowsys.funlet

import org.scalatest._
import org.scalatest.matchers._

class ParamsParserTest extends FlatSpec with ShouldMatchers {
    val parsedParams = MapParam(Map(
        "user" -> MapParam(Map(
            "name" -> StringParam("teamon"),
            "email" -> StringParam("i@teamon.eu"),
            "points" -> MapParam(Map(
                "a" -> StringParam("1"),
                "b" -> StringParam("2"),
                "c" -> StringParam("3")
            ))
        )),
        "post" -> MapParam(Map(
            "title" -> StringParam("Awesome parser"),
            "body" -> StringParam("Try it!")
        )),
        "day" -> StringParam("today"),
        "o rly" -> StringParam("Ya rly!"),
        "multiple" -> ListParam(
            StringParam("one") ::
            StringParam("two") ::
            Nil
        )
    ))

    val rawParams = Map(
        "user[name]" -> List("teamon"),
        "user[email]" -> List("i@teamon.eu"),
        "user[points][a]" -> List("1"),
        "user[points][b]" -> List("2"),
        "user[points][c]" -> List("3"),
        "post[title]" -> List("Awesome parser"),
        "post[body]" -> List("Try it!"),
        "day" -> List("today"),
        "o rly" -> List("Ya rly!"),
        "multiple[]" -> List("one", "two")
    )

    it should "encode params" in {
        ParamsParser.encode(parsedParams) should equal (rawParams)
    }

    it should "decode params" in {
        ParamsParser.decode(rawParams) should equal (parsedParams)
    }

}
