package com.verknowsys.served.web.snippet.configuration

import net.liftweb.util._
import net.liftweb.util.Helpers._
import net.liftweb.http._
import net.liftweb.http.js._

import scala.xml._

import com.verknowsys.served.web.lib.Session
import com.verknowsys.served.api._

object AddLoggerEntry extends LiftScreen {
    final val loggerLevels = Map(
        "error" -> Logger.Levels.Error,
        "warn"  -> Logger.Levels.Warn,
        "info"  -> Logger.Levels.Info,
        "debug" -> Logger.Levels.Debug,
        "trace" -> Logger.Levels.Trace
    )
    
    final val loggerLevelsList = loggerLevels.values.toList
    
    val className = field("Class name", "", 
                        trim,
                        valMinLen(1, "Class name can not be blank"))
                        
    
    val level = select("Level", loggerLevelsList.head, loggerLevelsList)
    
    override def finishButton: Elem = <button>{"Add entry"}</button>  
    
    
    def finish() {
        Session.api.request(Logger.AddEntry(className.is, level.is)){
            case Success => S.notice("Logger level " + level.is + " set for class " + className.is)
        }
    }
}

class Logging {
    def listEntries = {
        val entries = Session.api.request(Logger.ListEntries){ case Logger.Entries(entries) => entries.toList }.getOrElse(Nil)
        ".row *" #> entries.map { entry =>
            val id = nextFuncName
            ".className [id]" #> id &
            ".className *"  #> entry._1 &
            ".level *"      #> entry._2.toString &
            ".remove *"     #> SHtml.ajaxButton(Text("Remove"), () => {
                Session.api.request(Logger.RemoveEntry(entry._1)){
                    case Success => JE.JsRaw("$('#"+id+"').parent().remove()").cmd
                }.getOrElse(JsCmds.Noop)
            })
        }
    }
}

