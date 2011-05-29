package com.verknowsys.served.web.snippet

import net.liftweb.util._
import net.liftweb.util.Helpers._
import net.liftweb.http._
import net.liftweb.http.js._

import scala.xml._

import com.verknowsys.served.web.lib.Session
import com.verknowsys.served.api._

object Log extends net.liftweb.common.Logger



class Logging {
    final val loggerLevels = Map(
        "error" -> Logger.Levels.Error,
        "warn"  -> Logger.Levels.Warn,
        "info"  -> Logger.Levels.Info,
        "debug" -> Logger.Levels.Debug,
        "trace" -> Logger.Levels.Trace
    )
    
    
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
    
    
    def manage(xhtml: NodeSeq): NodeSeq = {
        var className = ""
        var level = ""
        
        def processEntry() = {
            // Simple validation
            if(!className.isEmpty) {
                loggerLevels.get(level.toLowerCase) match {
                    case Some(lvl) =>
                        Session.api.request(Logger.AddEntry(className, lvl)){
                            case Success => S.notice("Logger level " + lvl + " set for class " + className)
                        }
                    case None =>
                        S.error("Invalid logging level")
                }
            } else {
                S.error("Empty class name")
            }
        }
        
        bind("entry", xhtml,
            "className" -> SHtml.text(className, className = _),
            "level"     -> SHtml.text(level, level = _),
            "submit"    -> SHtml.submit("Add entry", processEntry)
        )
    }
}

