package com.verknowsys.served.web.endpoints

import com.verknowsys.funlet._
import com.verknowsys.served.web.lib._
import com.verknowsys.served.web.forms._
import com.verknowsys.served.api._
import com.verknowsys.served.api.git._

object GitEndpoint extends Endpoint {
    def routes(implicit req: Request) = {
        case Get("git" :: Nil) =>
            render("git/index", "repos" -> listRepos, "form" -> new RepositoryForm())

        case Post("git" :: Nil) =>
            val form = new RepositoryForm(param = formParam)
            if(form.isValid){
                API ! CreateRepository(form.get.name)
                // flash("notice") = "Repository created"
                redirect("/git")
            } else {
                render("git/index", "repos" -> listRepos, "form" -> form)
            }

        case Get("git" :: name :: Nil) =>
            find(name) map { repo =>
                render("git/show", "repo" -> repo, "form" -> new AccessKeyForm(action = "/git/" + repo.name + "/keys"))
            }



        case Post("git" :: name :: "keys" :: Nil) =>
            find(name) map { repo =>
                val form = new AccessKeyForm(param = formParam, action = "/git/" + repo.name + "/keys")
                if(form.isValid){
                    API ! AddAuthorizedKey(repo.uuid, form.get)
                    // flash("notice") = "Key added"
                    redirect("/git/" + repo.name, session = "success" -> "KeyAdded")
                } else {
                    // flash("alert") = "Invalid stuff"
                    render("git/show", "repo" -> repo, "form" -> form)
                }
            }
    }

    protected def find(name: String) = GetRepositoryByName(name) <> { case Some(r: Repository) => r }

    protected def listRepos = ListRepositories <> { case Repositories(repos) => repos } getOrElse Nil

    class RepositoryForm(repo: Option[Repository] = None, param: Param = Empty, action: String = "") extends Form[Repository](repo, param, action){
        def bind = name map (e => Repository(e))

        val name = new StringField("name", _.name, NotEmpty)

        def fields = name :: Nil
    }
}
