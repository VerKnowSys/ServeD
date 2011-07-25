package com.verknowsys.forms

import scala.xml.NodeSeq

trait Validators {
    val NotEmpty: Validator[String] = s => if(s.isEmpty) Some("Must not be empty") else None
    def LessThan(v: Int): Validator[Int] = i => if(i < v) None else Some("Must be less than " + v)
    def GreaterThan(v: Int): Validator[Int] = i => if(i > v) None else Some("Must be greater than " + v)

    def LessThanEqual(v: Int): Validator[Int] = i => GreaterThan(i)(v)
    def GreaterThanEqual(v: Int): Validator[Int] = i => LessThan(i)(v)
}

trait CommonFields {
    self: Form[_] =>

    class StringField(name: String, getter: Entity => String, validators: Validator[String]*)(implicit form: Form[Entity]) extends Field(name, getter, validators:_*){
        def decode(param: String) = Some(param)
    }

    class IntField(name: String, getter: Entity => Int, validators: Validator[Int]*)(implicit form: Form[Entity]) extends Field(name, getter, validators:_*){
        def decode(param: String) = try { Some(param.toInt) } catch { case ex: java.lang.NumberFormatException => None }
    }

    class SelectField[T](name: String, getter: Entity => T, values: Seq[T])(implicit form: Form[Entity]) extends Field[Entity, T](name, getter){
         val valuesMap = values map { v => (encode(v), v) } toMap

         def decode(param: String) = valuesMap.get(param)

         override def encode(value: T) = {
             import java.security._
             import java.math._

             val s = value.toString
             val m = MessageDigest.getInstance("MD5")
             m.update(s.getBytes, 0, s.length)
             new BigInteger(1, m.digest).toString(16)
         }

         override def inputHtml =
             <select name={fieldName} id={fieldId}>
                 {fieldOptions}
             </select>

        def fieldOptions = values map { e =>
            if(Some(e) == value) <option value={encode(e)} selected="selected">{e}</option>
            else <option value={encode(e)}>{e}</option>
        }
    }
}

abstract trait BaseForm {
    def toHtml: NodeSeq
}

abstract class Form[E](val entity: Option[E], val params: Params, val action: String = "", val method: String = "post") extends BaseForm with Validators with CommonFields {
    type Entity = E

    def bind: Option[Entity]

    def fields: Seq[Field[Entity, _]]

    def isValid = fields.forall(_.isValid)

    def isSubmitted = !params.isEmpty

    def value = bind orElse entity

    implicit val self = this

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
