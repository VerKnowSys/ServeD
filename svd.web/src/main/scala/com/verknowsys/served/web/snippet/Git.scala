package com.verknowsys.served.web.snippet

import net.liftweb.util._
import net.liftweb.util.Helpers._
import net.liftweb.http._
import net.liftweb.http.js._

import scala.xml._

import com.verknowsys.served.web.lib.Session
import com.verknowsys.served.api.git._
import com.verknowsys.served.api.Success

object AddRepository extends LiftScreen {
    val name = field("Name", "", trim, valMinLen(1, "Name can not be blank"))
    
    override def finishButton: Elem = <button>{"Add repository"}</button>  
    
    def finish() {
        Session.api.request(CreateRepository(name.is)){
            case repo: Repo => S.notice("Repository " + repo.name + " created")
        }
    }
}

class GitSnippet {
    def listRepositories = {
        val repositories = Session.api.request(ListRepositories){ case Repositories(repositories) => repositories }.getOrElse(Nil)
        ".row *" #> repositories.map { repo =>
            val id = nextFuncName
            ".name [id]" #> id &
            ".name *"  #> repo.name &
            ".remove *"     #> SHtml.ajaxButton(Text("Remove"), () => {
                Session.api.request(RemoveRepository(repo.uuid)){
                    case Success => JE.JsRaw("$('#"+id+"').parent().remove()").cmd
                }.getOrElse(JsCmds.Noop)
            })
        }
    }
}
