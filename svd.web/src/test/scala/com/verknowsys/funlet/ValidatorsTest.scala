package com.verknowsys.funlet

import org.scalatest._
import org.scalatest.matchers._


trait TestValidators {
    val Pass: Validator[Int] = e => Nil
    val FailA: Validator[Int] = e => List("Fail A")
    val FailB: Validator[Int] = e => List("Fail B")
    val v = 1
}

class ValidatorsTest extends FlatSpec with ShouldMatchers with TestValidators {
    "& operator" should "work" in {
        (Pass  & Pass)(v) should be (Nil)
        (FailA & Pass)(v) should be (List("Fail A"))
        (Pass  & FailB)(v) should be (List("Fail B"))
        (FailA & FailB)(v) should be (List("Fail A"))
    }

    "| operator" should "work" in {
        (Pass  | Pass)(v) should be (Nil)
        (FailA | Pass)(v) should be (Nil)
        (Pass  | FailB)(v) should be (Nil)
        (FailA | FailB)(v) should be (List("Fail B"))
    }

    "^ operator" should "work" in {
        (Pass  ^ Pass)(v) should not be ('empty)
        (FailA ^ Pass)(v) should be (Nil)
        (Pass  ^ FailB)(v) should be (Nil)
        (FailA ^ FailB)(v) should be (List("Fail A", "Fail B"))
    }
}
