package com.verknowsys.served.web.lib

import net.liftweb.http._
import net.liftweb.util._
import net.liftweb.util.Helpers._

import scala.xml.NodeSeq

object Crud {
    trait Base extends MVCHelper {
        type Entity

        implicit def CssSel2Option(sel: CssSel) = Some(sel)

        val Prefix: String

        def linkTo(label: String, param: String) =
            <a href={"/" + Prefix + "/" + param}>{label}</a>

        protected def render(opt: Option[CssSel], tplName: String) =
            Templates(Prefix :: tplName :: Nil).flatMap(t => opt.map(_(t)))
    }

    trait Index extends Base {
        def index: Option[CssSel]

        serve {
            case Prefix :: Nil => render(index, "index")
        }
    }

    trait Show extends Base {
        object entity extends RequestVar[Option[Entity]](None)

        def find(param: String): Option[Entity]
        def show(obj: Entity): Option[CssSel]

        serve {
            case Prefix :: param :: Nil => find(param) map { e =>
                entity(Some(e))
                render(show(e), "show")
            }
        }

    }

    trait All extends Index with Show
}
