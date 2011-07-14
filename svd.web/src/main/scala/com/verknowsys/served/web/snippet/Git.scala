package com.verknowsys.served.web.snippet

import net.liftweb.util._
import net.liftweb.util.Helpers._
import net.liftweb.http._
import net.liftweb.http.js._

import scala.xml._

import com.verknowsys.served.web.lib._
import com.verknowsys.served.api.git._
import com.verknowsys.served.api.Success
import com.verknowsys.served.utils.Logging


object AddRepository extends LiftScreen {
    val name = field("Name", "", trim, valMinLen(1, "Name can not be blank"))

    override def finishButton: Elem = <button>{"Add repository"}</button>

    def finish() {
        $(CreateRepository(name.is)){
            case repo: Repository =>
                S.notice("Repository " + repo.name + " created")
        }
    }
}

class GitSnippet extends Logging {
    def listRepositories = {
        log.trace("Listing repositories")
        val repositories = $(ListRepositories){ case Repositories(repositories) => repositories }.getOrElse(Nil)
        ".row *" #> repositories.map { repo =>
            val id = nextFuncName
            ".name [id]" #> id &
            ".name *"  #> repo.name &
            ".remove *"     #> SHtml.a(() => {
                $(RemoveRepository(repo.uuid)){
                    case Success => JE.JsRaw("$('#"+id+"').parent().remove()").cmd
                }.getOrElse(JsCmds.Noop)
            }, Text("Remove"))
        }
    }
}
