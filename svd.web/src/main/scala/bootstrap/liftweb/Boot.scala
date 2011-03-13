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
    def boot {
    // where to search snippet
    LiftRules.addToPackages("com.verknowsys.served.web")

    // Build SiteMap

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

    val entries =
        Menu(Loc("home", "index" :: Nil, "Home", Authorized)) ::
        Menu(Loc("login", "login" :: Nil, "Login", Unauthorized)) ::
        Menu(Loc("logout", "logout" :: Nil, "Logout", EarlyResponse(() => {
            Session.logout
            Full(RedirectWithState("/",
                RedirectState(() => S.notice("Logged out"))
            ))
        }), Authorized)) ::
        Nil

        // more complex because this menu allows anything in the
        // /static path to be visible
        // Menu(Loc("Static", Link(List("static"), true, "/static/index"), "Static Content"))

    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    LiftRules.setSiteMap(SiteMap(entries:_*))

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart = Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    
    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd = Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))
  }
}
