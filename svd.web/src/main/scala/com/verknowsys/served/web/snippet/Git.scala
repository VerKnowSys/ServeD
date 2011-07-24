package com.verknowsys.served.web.snippet

import net.liftweb.util._
import net.liftweb.util.Helpers._
import net.liftweb.http._
import net.liftweb.http.js._
import net.liftweb.sitemap._

import net.liftweb.http.js.jquery._

import scala.xml._

import com.verknowsys.served.web.lib._
import com.verknowsys.served.api.git._
import com.verknowsys.served.api.Success
import com.verknowsys.served.utils.Logging
import com.verknowsys.served.utils.KeyUtils
import java.security.PublicKey


import net.liftweb.common.Full


class GitSnippet extends Logging {
    def index = {
        val repositories = ListRepositories <> { case Repositories(repositories) => repositories } getOrElse Nil
        ".row *" #> repositories.map { repo =>
            val id = nextFuncName
            ".name [id]"    #> id &
            ".name *"       #> SHtml.ajaxButton(repo.name, () => {
                returnContent("git" :: "show" :: Nil){
                    "#name"   #> repo.name
                    // &
                    // "li *"    #> repo.authorizedKeys.map { e =>
                    //     ".name *"   #> e.toString
                    // }
                }
            }) &
            ".remove *"     #> SHtml.ajaxButton("Remove", () => {
                RemoveRepository(repo.uuid) <> {
                    case Success => JE.JsRaw("$('#"+id+"').parent().remove()").cmd
                } getOrElse JsCmds.Noop
            })
        }
    }

    def returnContent(template: List[String])(sel: CssSel) =
        Templates(template).map { tpl => JqJsCmds.JqSetHtml("content", sel(tpl)) } getOrElse JsCmds.Noop
}





object AddRepository extends LiftScreen {
    val name = field("Name", "", trim, valMinLen(1, "Name can not be blank"))

    override def finishButton: Elem = <button>{"Add repository"}</button>

    def finish() {
        CreateRepository(name.is) <> {
            case repo: Repository =>
                S.notice("Repository " + repo.name + " created")
        }
    }
}

trait CustomScreen extends LiftScreen {
    def ensureSome[T](msg: => String): Option[T] => List[FieldError] = _ match {
        case Some(obj) => Nil
        case _ => msg
    }
}

// class AddKey {
//     def render = Text("dupa")
// }

class AddKey extends CustomScreen with Logging {
    val keyOpt: Field { type ValueType = Option[PublicKey] } = field("Public Key", None, ensureSome("Invalid public key"))

    object repoOpt extends ScreenVar[Option[Repository]](None)

    // override def localSetup(){
    //     repoOpt(GitController.entity)
    // }

    def finish {
        log.trace("repo: " + repoOpt)
        log.trace("key: " + keyOpt)
        // for(repo <- repoOpt;
        //     key <- keyOpt) yield {
        //
        //     AddAuthorizedKey(repo.uuid, key) <> {
        //         case Success => S.notice("Key added")
        //     }
        // } getOrElse {
        //     S.error("Invalid key")
        // }
    }
}

class GitRepositoryKeys {
    def render = {
        "li *" #> List(1,3,4).map { e =>
            ".name *" #> e.toString
        }
    }
}

object GitController extends Crud.All with Logging {
    type Entity = Repository
    val Prefix = "git"

    def menus =
        (Menu("Git") / "git") ::
        (Menu("Git show") / "git" / * >> Loc.Hidden) ::
        Nil

    def find(name: String) = GetRepositoryByName(name) <> { case Some(repo: Repository) => repo }
    def show(repo: Repository) = {
        "#name" #> repo.name
    }

    def index = {
        val repositories = ListRepositories <> { case Repositories(repositories) => repositories } getOrElse Nil
        ".row *" #> repositories.map { repo =>
            val id = nextFuncName
            ".name [id]"    #> id &
            ".name *"       #> linkTo(repo.name, repo.name) &
            ".remove *"     #> SHtml.a(() => {
                RemoveRepository(repo.uuid) <> {
                    case Success => JE.JsRaw("$('#"+id+"').parent().remove()").cmd
                } getOrElse JsCmds.Noop
            }, Text("Remove"))
        }
    }
}
