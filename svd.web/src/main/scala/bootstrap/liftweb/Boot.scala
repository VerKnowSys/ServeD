package bootstrap.liftweb

import net.liftweb._
import net.liftweb.util._
import net.liftweb.util.Helpers._
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._

import com.verknowsys.served.web.lib.Session
import com.verknowsys.served.web.snippet.GitController
import com.verknowsys.served.utils.Logging

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot extends Logging {
    val Authorized = If(
        () => Session.authorized,
        () => RedirectWithState(
            "/login",
            RedirectState(() => S.notice("Please log in first"))
        )
    )

    val Unauthorized = Unless(
        () => Session.authorized,
        () => RedirectResponse("/")
    )

    def siteMap = SiteMap(
        Menu("Home") / "index" >> Authorized,
        // Menu(Loc("git", ("git" :: Nil) -> true, "Git", Authorized)),

        Menu("Monitoring") / "monitoring" >> Authorized submenus (
            Menu("Akka actors") / "monitoring" / "akka_actors",
            Menu("CPU usage")   / "monitoring" / "cpu"
        ),
        Menu("Configuration") / "configuration" >> Authorized submenus (
            Menu("Logging") / "configuration" / "logging"
        ),

        Menu("Log in") / "login" >> Unauthorized,
        Menu(Loc("logout", "logout" :: Nil, "Log out", EarlyResponse(() => {
            Session.logout
            Full(RedirectWithState("/",
                RedirectState(() => S.notice("Logged out"))
            ))
        }), Authorized, Hidden)),

        (Menu("Git") / "git") >> Authorized,
        (Menu("Git show") / "git" / * >> Hidden >> Authorized)
    )

    def boot {
        // where to search snippet
        LiftRules.addToPackages("com.verknowsys.served.web")

        LiftRules.setSiteMap(siteMap)

        LiftRules.dispatch.append(GitController)



        // Ajax loader
        LiftRules.ajaxStart = Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
        LiftRules.ajaxEnd = Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

        // Force the request to be UTF-8
        LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

        // Use scoped snippets
        LiftRules.snippetNamesToSearch.default.set((s: String) => LiftRules.searchSnippetsWithRequestPath(s))
    }
}
