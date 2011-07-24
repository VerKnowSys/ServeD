package com.verknowsys.served.web.snippet.configuration

import net.liftweb.util._
import net.liftweb.util.Helpers._
import net.liftweb.http._
import net.liftweb.http.js._
import net.liftweb.common.Full

import scala.xml._

import com.verknowsys.served.web.lib.Session
import com.verknowsys.served.api._
import com.verknowsys.served.utils


object AddLoggerEntry extends LiftScreen {
    final val loggerLevelsList = Logger.Levels.values.toList
    
    val className = field("Class name", "", trim, valMinLen(1, "Class name can not be blank"))
    val level = select("Level", loggerLevelsList.head, loggerLevelsList)
    
    override def finishButton: Elem = <button>{"Add entry"}</button>  
    
    def finish() {
        Session.api.request(Logger.AddEntry(className.is, level.is)){
            case Success => S.notice("Logger level " + level.is + " set for class " + className.is)
        }
    }
}

class Logging extends utils.Logging {
    def listEntries = {
        val entries = Session.api.request(Logger.ListEntries){ case Logger.Entries(entries) => entries.toList }.getOrElse(Nil)
        ".row *" #> entries.map { case(name, level) =>
            val id = nextFuncName
            ".className [id]" #> id &
            ".className *"  #> name &
            ".level *"     #> SHtml.ajaxSelectElem(Logger.Levels.values.toList, Full(level)){ level =>
                Session.api.request(Logger.AddEntry(name, level)){ case Success => }
                JsCmds.Noop
            } &
            ".remove *"     #> SHtml.ajaxButton(Text("Remove"), () => {
                Session.api.request(Logger.RemoveEntry(name)){
                    case Success => JE.JsRaw("$('#"+id+"').parent().remove()").cmd
                }.getOrElse(JsCmds.Noop)
            })
        }
    }
}

