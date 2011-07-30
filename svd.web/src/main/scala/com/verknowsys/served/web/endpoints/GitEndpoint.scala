package com.verknowsys.served.web.endpoints

import com.verknowsys.served.web.lib._
import com.verknowsys.served.api.git._
import com.verknowsys.forms._

trait GitEndpoint {
    self: Endpoint =>

    class RepositoryForm(repo: Option[Repository] = None, params: Params = Params.Empty) extends Form[Repository](repo, params){
        def bind = name map (e => Repository(e))

        val name = new StringField("name", _.name, NotEmpty)

        def fields = name :: Nil
    }

    // class PublicKeyForm()

    get("/git"){
        render("git/index", "repos" -> listRepos, "form" -> new RepositoryForm())
    }

    post("/git"){
        val form = new RepositoryForm(params = formParams)
        if(form.isValid){
            API ! CreateRepository(form.get.name)
            redirect("/git")
        } else {
            render("git/index", "repos" -> listRepos, "form" -> form)
        }
    }

    get("/git/:name"){
        find(params("name")) map { repo =>
            render("git/show", "repo" -> repo/*, "form" -> new PublicKeyForm()*/)
        } getOrElse notFound()
    }

    protected def find(name: String) = GetRepositoryByName(name) <> { case Some(r: Repository) => r }

    protected def listRepos = ListRepositories <> { case Repositories(repos) => repos } getOrElse Nil
}
