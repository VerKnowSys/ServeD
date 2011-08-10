package com.verknowsys.served.web.forms

import com.verknowsys.funlet._
import com.verknowsys.served.api.AccessKey

class AccessKeyForm(accessKey: Option[AccessKey] = None, param: Param = Empty, action: String = "") extends Form[AccessKey](accessKey, param, action) with CustomFields {
    def bind = for {
        n <- name
        k <- publicKey
    } yield AccessKey(n, k)

    val name = new StringField("name", _.name, NotEmpty)
    val publicKey = new PublicKeyField("publicKey", _.key)

    def fields = name :: publicKey :: Nil
}
