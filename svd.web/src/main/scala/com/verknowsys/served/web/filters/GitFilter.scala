package com.verknowsys.served.web.filters

import com.verknowsys.served.web.lib._
import com.verknowsys.served.api.git._

import com.verknowsys.forms._

class GitFilter extends IndexAction {
    def prefix = "git"
    // class LoggerEntryForm(entry: Option[AddEntry] = None, params: Params = Params.Empty) extends Form[AddEntry](entry, params, createPath){
    //     def bind = for {
    //         cn <- className
    //         l <- level
    //     } yield AddEntry(cn, l)
    //
    //     val className = new StringField("className", _.className, NotEmpty)
    //     val level = new SelectField("level", _.level, Levels.values.toSeq)
    //
    //     def fields = className :: level :: Nil
    // }
    //
    def index = {
        Map("repos" -> listRepos)
    }
    //
    // def create = {
    //     val form = new LoggerEntryForm(params = formParams)
    //
    //     if(form.isValid) {
    //         API ! form.value.get
    //         redirect(indexPath)
    //     } else {
    //         render("index", Map("entries" -> listEntries, "form" -> form))
    //     }
    // }

    protected def listRepos = ListRepositories <> { case Repositories(repos) => repos } getOrElse Nil
}
