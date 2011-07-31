package com.verknowsys.funlet

import scala.xml.NodeSeq

trait Validators {
    val NotEmpty: Validator[String] = s => if(s.isEmpty) Some("Must not be empty") else None
    def LessThan(v: Int): Validator[Int] = i => if(i < v) None else Some("Must be less than " + v)
    def GreaterThan(v: Int): Validator[Int] = i => if(i > v) None else Some("Must be greater than " + v)

    def LessThanEqual(v: Int): Validator[Int] = i => GreaterThan(i)(v)
    def GreaterThanEqual(v: Int): Validator[Int] = i => LessThan(i)(v)
}

abstract trait BaseForm {
    def toHtml: NodeSeq
}

abstract class Form[E](val entity: Option[E], param: Param, val action: String = "", val method: String = "post") extends BaseForm with Validators with CommonFields {
    type Entity = E

    def bind: Option[Entity]

    def fields: Seq[Field[Entity, _]]

    def isValid = fields.forall(_.isValid)

    def isSubmitted = !params.isEmpty

    def value = bind orElse entity

    def get = value.get

    implicit val self = this

    lazy val params: Map[String, String] = param match {
        case MapParam(map) => map.collect { case(k, StringParam(v)) => (k,v) }
        case _ => Map()
    }

    def submitButton =
        <div class="form-row form-submit">
            <input type="submit" value="Submit" />
        </div>

    def toHtml =
        <form action={action} method={method}>
            {fields map (_.toHtml)}
            {submitButton}
        </form>
}
