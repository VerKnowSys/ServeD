package com.verknowsys.funlet

import java.security._
import java.math._

abstract class Field[Entity, T](val name: String, getter: Entity => T, validators: Validator[T]*)(implicit val form: Form[Entity]) {
    def decode(param: String): Option[T]
    def encode(value: T): String = value.toString

    lazy val (value, errors) = calculateValue

    def calculateValue: (Option[T], Seq[String]) = {
        val value = form.params.get(name).map(decode) getOrElse form.entity.map(getter)
        val errors = value match {
            case Some(v) => validators.view map { _(v) } collect { case Some(e) => e }
            case None if form.isSubmitted => List("Is invalid")
            case _ => Nil
        }

        (value, errors)
    }

    def isValid = value.isDefined && errors.isEmpty

    // Default form display

    def fieldId = "form-" + name

    def fieldName = "form[" + name + "]"

    def labelText = name.capitalize

    def labelHtml = <label for={fieldId}>{labelText}</label>

    def fieldValue = value map encode getOrElse ""

    def inputHtml = <input type="text" name={fieldName} id={fieldId} value={fieldValue} />

    def divClass = "form-row" + (if(form.isSubmitted && !isValid) " form-field-with-errors" else "")

    def fieldErrors = errors.map { e => errorField(e) }

    def errorField(msg: String) = <span class="form-error">{msg}</span>

    def toHtml =
        <div class={divClass}>
            {labelHtml}
            {inputHtml}
            {fieldErrors}
        </div>

    protected def hashValue(s: String) = {
        val m = MessageDigest.getInstance("MD5")
        m.update(s.getBytes, 0, s.length)
        new BigInteger(1, m.digest).toString(16)
    }
}

