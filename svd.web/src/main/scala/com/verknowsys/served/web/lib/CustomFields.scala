package com.verknowsys.served.web.lib

import com.verknowsys.funlet._
import java.security.PublicKey
import com.verknowsys.served.utils.KeyUtils

trait CustomFields {
    self: Form[_] =>

     class PublicKeyField(name: String, getter: Entity => PublicKey)(implicit form: Form[Entity]) extends Field[Entity, PublicKey](name, getter){
        def decode(param: String) = KeyUtils.load(param)

        override def inputHtml = <textarea name={fieldName} id={fieldId}>{fieldValue}</textarea>
    }
}
