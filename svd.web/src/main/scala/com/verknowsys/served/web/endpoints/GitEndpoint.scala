package com.verknowsys.served.web.endpoints

import com.verknowsys.served.web.lib._
import com.verknowsys.served.api.git._
import com.verknowsys.served.utils.KeyUtils
import com.verknowsys.forms._
import java.security.PublicKey

trait GitEndpoint {
    self: Endpoint =>

    class RepositoryForm(repo: Option[Repository] = None, params: Params = Params.Empty) extends Form[Repository](repo, params){
        def bind = name map (e => Repository(e))

        val name = new StringField("name", _.name, NotEmpty)

        def fields = name :: Nil
    }

    class PublicKeyForm(accessKey: Option[AccessKey] = None, params: Params = Params.Empty, action: String = "") extends Form[AccessKey](accessKey, params, action) {
        def bind = for {
            n <- name
            k <- publicKey
        } yield AccessKey(n, k)

        val name = new StringField("name", _.name, NotEmpty)
        val publicKey = new PublicKeyField("publicKey", _.key)

        def fields = name :: publicKey :: Nil
    }

    get("/git"){
        render("git/index", "repos" -> listRepos, "form" -> new RepositoryForm())
    }

    post("/git"){
        val form = new RepositoryForm(params = formParams)
        if(form.isValid){
            API ! CreateRepository(form.get.name)
            flash("notice") = "Repository created"
            redirect("/git")
        } else {
            render("git/index", "repos" -> listRepos, "form" -> form)
        }
    }

    get("/git/:name"){
        find(params("name")) map { repo =>
            render("git/show", "repo" -> repo, "form" -> new PublicKeyForm(action = "/git/" + repo.name + "/keys"))
        } getOrElse notFound()
    }

    post("/git/:name/keys"){
        find(params("name")) map { repo =>
            val form = new PublicKeyForm(params = formParams, action = "/git/" + repo.name + "/keys")
            if(form.isValid){
                API ! AddAuthorizedKey(repo.uuid, form.get)
                flash("notice") = "Key added"
                redirect("/git/" + repo.name)
            } else {
                flash("alert") = "Invalid stuff"
                render("git/show", "repo" -> repo, "form" -> form)
            }
        } getOrElse notFound()
    }

    protected def find(name: String) = GetRepositoryByName(name) <> { case Some(r: Repository) => r }

    protected def listRepos = ListRepositories <> { case Repositories(repos) => repos } getOrElse Nil
}
