package bootstrap.liftweb

import net.liftweb._
import net.liftweb.util._
import net.liftweb.util.Helpers._
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._

import com.verknowsys.served.web.lib.Session

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
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
        Menu(S ? "Home") / "index" >> Authorized,
        Menu(S ? "Monitoring") / "monitoring" >> Authorized submenus (
            Menu(S ? "Akka actors") / "monitoring" / "akka_actors",
            Menu(S ? "CPU usage")   / "monitoring" / "cpu"
        ),

        Menu(S ? "Log in") / "login" >> Unauthorized,
        Menu(Loc("logout", "logout" :: Nil, "Log out", EarlyResponse(() => {
            Session.logout
            Full(RedirectWithState("/",
                RedirectState(() => S.notice("Logged out"))
            ))
        }), Authorized, Hidden))
    )
    
    def boot {
        // where to search snippet
        LiftRules.addToPackages("com.verknowsys.served.web")

        LiftRules.setSiteMap(siteMap)

        // Ajax loader
        LiftRules.ajaxStart = Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
        LiftRules.ajaxEnd = Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

        // Force the request to be UTF-8
        LiftRules.early.append(_.setCharacterEncoding("UTF-8"))
    }
}
