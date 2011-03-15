package com.verknowsys.served.web.snippet

import net.liftweb.util._
import net.liftweb.util.Helpers._
import net.liftweb.common._

import net.liftweb.json.JsonAST
import net.liftweb.json.JsonDSL._


import net.liftweb._
import http._
import js._
import JsCmds._
import JE._


class LogScript {
    def render = Script(JsRaw("var LogData = " + collectData))
    
    val hasStarted = ".+: \\[Actor\\[(.+?):(.+?)\\]\\] has started".r
    val linking = ".+: Linking actor \\[Actor\\[(.+?):(.+?)\\]\\] to actor \\[Actor\\[(.+?):(.+?)\\]\\]".r
    
    def short(cls: String) = cls.split("\\.").last
    
    protected def collectData = compact(JsonAST.render(
        io.Source.fromFile("logs/akka.log").getLines.collect { 
            case hasStarted(cls, id) => 
                ("ev" -> "started") ~ ("id" -> id) ~ ("cls" -> cls.split("\\.").last)
                
            case linking(acls, aid, bcls, bid) => 
                ("ev" -> "linked") ~ ("aid" -> aid) ~ ("acls" -> short(acls)) ~ ("bid" -> bid) ~ ("bcls" -> short(bcls))
        }.toList
    ))



        // class ActorInfo(cls: String, id: String){
        //     var state = State.New
        //     val linked = new ListBuffer[ActorInfo]
        //     var inTree = false
        // 
        //     def short = cls.split("\\.").last
        // }
        // 
        // object App {
        //     val display = new Display
        // 
        //     val actors = Map[String, ActorInfo]()

}
