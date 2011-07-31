package com.verknowsys.funlet

import scala.xml.NodeSeq
import java.security.PublicKey
import com.verknowsys.served.utils.KeyUtils

trait CommonFields {
    self: Form[_] =>

    class StringField(name: String, getter: Entity => String, validators: Validator[String]*)(implicit form: Form[Entity]) extends Field(name, getter, validators:_*){
        def decode(param: String) = Some(param)
    }

    class IntField(name: String, getter: Entity => Int, validators: Validator[Int]*)(implicit form: Form[Entity]) extends Field(name, getter, validators:_*){
        def decode(param: String) = try { Some(param.toInt) } catch { case ex: java.lang.NumberFormatException => None }
    }

    class PublicKeyField(name: String, getter: Entity => PublicKey)(implicit form: Form[Entity]) extends Field[Entity, PublicKey](name, getter){
        def decode(param: String) = KeyUtils.load(param)

        override def inputHtml = <textarea name={fieldName} id={fieldId}>{fieldValue}</textarea>
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
