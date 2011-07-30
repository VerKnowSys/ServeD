package com.verknowsys.served.web.endpoints

import com.verknowsys.served.web.lib._
import com.verknowsys.served.api.Logger._

import com.verknowsys.forms._

trait LoggerEndpoint {
    self: Endpoint =>

    class LoggerEntryForm(entry: Option[AddEntry] = None, params: Params = Params.Empty) extends Form[AddEntry](entry, params){
        def bind = for {
            cn <- className
            l <- level
        } yield AddEntry(cn, l)

        val className = new StringField("className", _.className, NotEmpty)
        val level = new SelectField("level", _.level, Levels.values.toSeq)

        def fields = className :: level :: Nil
    }

    get("/logger"){
        render("logger/index", "entries" -> listEntries, "form" -> new LoggerEntryForm())
    }

    post("/logger"){
        val form = new LoggerEntryForm(params = formParams)
        if(form.isValid){
            API ! form.get
            redirect("/logger")
        } else {
            render("logger/index", "entries" -> listEntries, "form" -> form)
        }
    }

    protected def listEntries = ListEntries <> { case Entries(entries) => entries } getOrElse Map()
}
