package com.verknowsys.served.web.endpoints

import com.verknowsys.funlet._
import com.verknowsys.served.web.lib._
import com.verknowsys.served.api.Logger._

object LoggerEndpoint extends Endpoint {
    def routes(implicit req: Request) = {
        case Get("logger" :: Nil) =>
            render("logger/index", "entries" -> listEntries, "form" -> new LoggerEntryForm())

        case Post("logger" :: Nil) =>
            val form = new LoggerEntryForm(param = formParam)
            if(form.isValid){
                API ! form.get
                redirect("/logger")
            } else {
                render("logger/index", "entries" -> listEntries, "form" -> form)
            }
    }

    protected def listEntries = ListEntries <> { case Entries(entries) => entries } getOrElse Map()

    class LoggerEntryForm(entry: Option[AddEntry] = None, param: Param = Empty) extends Form[AddEntry](entry, param){
        def bind = for {
            cn <- className
            l <- level
        } yield AddEntry(cn, l)

        val className = new StringField("className", _.className, NotEmpty)
        val level = new SelectField("level", _.level, Levels.values.toSeq)

        def fields = className :: level :: Nil
    }
}
