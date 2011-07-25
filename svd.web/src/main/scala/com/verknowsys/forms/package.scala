package com.verknowsys

package object forms {
    implicit def fieldToOption[E,T](field: Field[E,T]) = field.value

    type Params = Map[String, String]
    object Params {
        val Empty: Params = Map()
    }

    type Validator[T] = Function1[T, Option[String]]
}
