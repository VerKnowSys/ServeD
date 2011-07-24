package com.verknowsys.served.web.snippet.monitoring

import com.verknowsys.served.web.lib.Session
import com.verknowsys.served.api.Admin

import net.liftweb.json.NoTypeHints
import net.liftweb.json.Serialization
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._

class AkkaActors {
    // HACK: Somehow Json Serializer do not want to serialize Admin.ActorInfo. This is a simple stupid wrapper that just works.
    case class JsonFriendlyActorInfo(uuid: String, className: String, status: String, linkedActors: List[JsonFriendlyActorInfo]){
        def this(a: Admin.ActorInfo) = this(a.uuid, a.className, a.status, a.linkedActors.map(new JsonFriendlyActorInfo(_)))
    }
    
    def render = Script(JsRaw("var LogData = " + collectData))
    
    protected def collectData = {
        implicit val formats =  Serialization.formats(NoTypeHints)
        Serialization.write(actorsTree.map(new JsonFriendlyActorInfo(_)))
    }
    
    protected def actorsTree: List[Admin.ActorInfo] = Session.api.request(Admin.ListTreeActors){ case Admin.ActorsList(arr) => arr.toList }.getOrElse(Nil)
}
