package com.verknowsys.served.web.snippet

import com.verknowsys.served.web.lib.Session
import com.verknowsys.served.api.Admin

import net.liftweb.util._
import net.liftweb.util.Helpers._
import net.liftweb.common._

import net.liftweb.json.JsonAST
import net.liftweb.json.JsonDSL._
import net.liftweb.json.Printer.compact


import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._


class AkkaActors {
    // def render = Script(JsRaw("var LogData = " + collectData))
    // 
    // // val hasStarted = ".+: \\[Actor\\[(.+?):(.+?)\\]\\] has started".r
    // // val linking = ".+: Linking actor \\[Actor\\[(.+?):(.+?)\\]\\] to actor \\[Actor\\[(.+?):(.+?)\\]\\]".r
    // 
    // protected def short(cls: String) = cls.split("\\.").last
    // 
    // protected def collectData = compact(JsonAST.render(
    //     allActors.map { a =>
    //         a.supervisor match {
    //             case Some(s) =>
    //                 ("ev" -> "linked") ~ ("aid" -> a.uuid) ~ ("acls" -> short(a.cls)) ~ ("bid" -> s._1) ~ ("bcls" -> short(s._2))
    //             case None => 
    //                 ("ev" -> "started") ~ ("id" -> a.uuid) ~ ("cls" -> short(a.cls))
    //         }
    //     }
    //     
    //     // io.Source.fromFile("logs/akka.log").getLines.collect { 
    //     //     case hasStarted(cls, id) => 
    //     //         ("ev" -> "started") ~ ("id" -> id) ~ ("cls" -> cls.split("\\.").last)
    //     //         
    //     //     case linking(acls, aid, bcls, bid) => 
    //     //         ("ev" -> "linked") ~ ("aid" -> aid) ~ ("acls" -> short(acls)) ~ ("bid" -> bid) ~ ("bcls" -> short(bcls))
    //     // }.toList
    // ))
    
    def list = "li *" #> allActors.map(_.toString)
    
    
    
    protected def allActors = Session.api.request(Admin.ListActors){ case Admin.ActorsList(arr) => arr.toList }.getOrElse(Nil)
    
    protected def actorsTree = Session.api.request(Admin.ListTreeActors){ case Admin.ActorsList(arr) => arr.toList }.getOrElse(Nil)
}
