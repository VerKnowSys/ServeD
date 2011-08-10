package com.verknowsys.served.web.endpoints

import com.verknowsys.funlet._
import com.verknowsys.served.web.lib._
import com.verknowsys.served.web.forms.AccessKeyForm
import com.verknowsys.served.api.accountkeys._
import com.verknowsys.served.api.AccessKey

object AccountKeysEndpoint extends Endpoint {
    def routes(implicit req: Request) = {
        case Get("account-keys" :: Nil) => index(new AccessKeyForm())

        case Post("account-keys" :: Nil) =>
            val form = new AccessKeyForm(param = formParam)
            if(form.isValid){
                API ! AddKey(form.get)
                redirect("/account-keys", session = "success" -> "KeyAdded")
            } else {
                index(form)
            }
    }

    protected def index(form: AccessKeyForm)(implicit req: Request) = render("account-keys/index", "keys" -> listKeys, "form" -> form)

    protected def listKeys = ListKeys <> { case set: Set[AccessKey] => set } getOrElse Set.empty
}
