package com.verknowsys.served.web

import com.verknowsys.funlet._
import com.verknowsys.forms._

object GitEndpoint extends Endpoint {
    def routes(implicit req: Request) = {
        case Request(Get, "git" :: Nil) => <h1>Hello git</h1>
    }
}

import com.verknowsys.served.api.Logger._

object LoggerEndpoint extends Endpoint {
    def routes(implicit req: Request) = {
        case Request(Get,   "logger" :: Nil) => render("logger/index", "entries" -> listEntries, "form" -> new LoggerEntryForm())
        case Request(Post,  "logger" :: Nil) => <h1>Hello logger</h1>
    }

    protected def listEntries = /*ListEntries <> { case Entries(entries) => entries } getOrElse */ Map()

    class LoggerEntryForm(entry: Option[AddEntry] = None, params: Params = Params.Empty) extends Form[AddEntry](entry, params){
        def bind = for {
            cn <- className
            l <- level
        } yield AddEntry(cn, l)

        val className = new StringField("className", _.className, NotEmpty)
        val level = new SelectField("level", _.level, Levels.values.toSeq)

        def fields = className :: level :: Nil
    }
}

class Main extends MainEndpoint {
    override val endpoints = GitEndpoint :: LoggerEndpoint :: Nil

    override def routes(implicit req: Request) = {
        case Request(Get, path) => "Hello"
    }

}
