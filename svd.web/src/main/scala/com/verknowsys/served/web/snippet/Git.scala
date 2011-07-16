package com.verknowsys.served.web.snippet

import net.liftweb.util._
import net.liftweb.util.Helpers._
import net.liftweb.http._
import net.liftweb.http.js._
import net.liftweb.sitemap._

import scala.xml._

import com.verknowsys.served.web.lib._
import com.verknowsys.served.api.git._
import com.verknowsys.served.api.Success
import com.verknowsys.served.utils.Logging
import com.verknowsys.served.utils.KeyUtils
import java.security.PublicKey


import net.liftweb.common.Full


object AddRepository extends LiftScreen {
    val name = field("Name", "", trim, valMinLen(1, "Name can not be blank"))

    override def finishButton: Elem = <button>{"Add repository"}</button>

    def finish() {
        CreateRepository(name.is) <> {
            case repo: Repository =>
                S.redirectTo("/git", () => S.notice("Repository " + repo.name + " created"))
        }
    }
}

trait CustomScreen extends LiftScreen {
    def ensureSome[T](msg: => String) = (opt: Option[T]) => opt match {
        case Some(obj) => Nil
        case _ => List(FieldError(currentField.box openOr new FieldIdentifier {}, Text(msg)))
    }
}

object AddKey extends CustomScreen with Logging {
    val keyOpt: Field { type ValueType = Option[PublicKey] } = field("Public Key", None)

    def finish {
        log.trace("repo: " + GitController.entity)
        log.trace("key: " + keyOpt)
        for(repo <- GitController.entity;
            key <- keyOpt){

            AddAuthorizedKey(repo.uuid, key) <> {
                case Success =>
                    S.redirectTo("/git/" + repo.name, () => S.notice("Key added"))
            }
        }
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
