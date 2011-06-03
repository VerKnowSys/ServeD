package com.verknowsys.served.web.snippet

import net.liftweb.util._
import net.liftweb.util.Helpers._
import net.liftweb.http._
import net.liftweb.http.js._

import scala.xml._

import com.verknowsys.served.web.lib.Session
import com.verknowsys.served.api._

object AddRepository extends LiftScreen {
    val name = field("Name", "", 
                        trim,
                        valMinLen(1, "Name can not be blank"))
                        
    
    override def finishButton: Elem = <button>{"Add repository"}</button>  
    
    
    def finish() {
        Session.api.request(Git.CreateRepository(name.is)){
            case Success => S.notice("Repository " + name.is + "created")
        }
    }
}

class GitSnippet {
    def listRepositories = {
        val repositories = Session.api.request(Git.ListRepositories){ case Git.Repositories(repositories) => repositories }.getOrElse(Nil)
        ".row *" #> repositories.map { name =>
            val id = nextFuncName
            ".name [id]" #> id &
            ".name *"  #> name &
            ".remove *"     #> SHtml.ajaxButton(Text("Remove"), () => {
                Session.api.request(Git.RemoveRepository(name)){
                    case Success => JE.JsRaw("$('#"+id+"').parent().remove()").cmd
                }.getOrElse(JsCmds.Noop)
            })
        }
    }
}

