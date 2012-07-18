package com.verknowsys.funlet

import scala.xml.{NodeSeq, Null}

abstract trait BaseForm {
    def toHtml: NodeSeq
}

abstract class Form[E](val entity: Option[E], param: Param, val action: String = "", val method: HttpMethod = Post) extends BaseForm with Validators with CommonFields {
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

    val (realMethod, fakeMethod) = method match {
        case m @ (Get | Post) => (m.name, Null)
        case m => (Post.name, <input type="hidden" value={"_" + m.name}/>)
    }

    def submitButton =
        <div class="form-row form-submit">
            <input type="submit" value="Submit" />
        </div>

    def toHtml =
        <form action={action} method={realMethod}>
            {fakeMethod}
            {fields map (_.toHtml)}
            {submitButton}
        </form>
}
