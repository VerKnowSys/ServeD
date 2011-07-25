package com.verknowsys.served.web.filters

import com.verknowsys.served.web.lib._
import com.verknowsys.served.api.Logger._

import com.verknowsys.forms._

case class Entry(className: String, level: Levels.Value)



class LoggerFilter extends REST {
    def prefix = "logger"

    class LoggerEntryForm(entry: Option[Entry] = None, params: Params = Params.Empty) extends Form[Entry](entry, params, createPath){
        def bind = for {
            cn <- className
            l <- level
        } yield Entry(cn, l)

        val className = new StringField("className", _.className, NotEmpty)
        val level = new SelectField("level", _.level, Levels.values.toSeq)

        def fields = className :: level :: Nil
    }

    def index = {
        val form = new LoggerEntryForm()
        Map("entries" -> listEntries, "form" -> form)
    }

    def create = {
        val form = new LoggerEntryForm(params = formParams)

        if(form.isValid) {
            val entry = form.value.get
            log.info("Form valid")
            API ! AddEntry(entry.className, Levels.Trace)
            redirect(indexPath)
        } else {
            log.error("Form invalid")
            render("index", Map("entries" -> listEntries, "form" -> form))
        }
    }

    protected def listEntries = ListEntries <> { case Entries(entries) => entries } getOrElse Map()
}
