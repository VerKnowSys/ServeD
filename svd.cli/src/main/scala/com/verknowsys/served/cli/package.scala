package com.verknowsys.served

import com.verknowsys.served.api.ApiMessage

package object cli {
    type Args = Iterable[String]
    type Svd = akka.actor.ActorRef
    
    def request(msg: ApiMessage)(f: PartialFunction[Any, Unit])(implicit svd: Svd) {
        (svd !! msg) match {
            case Some(response) if f.isDefinedAt(response) => f(response)
            case Some(response) => error("Unhandled reponse %s", response)
            case None => error("Connection timeout")
        }
    }
    
    def confirm(msg: String) = {
        warn(msg)
        Console.readLine("[Y/n]: ") == "Y"
    }
    
    def quit {
        akka.actor.Actor.registry.shutdownAll
        sys.exit(0)
    }
    
    def print(list: Iterable[String]) = list.foreach(println)

    def info(msg: String, args: Any*) = log("", msg, args)
    def warn(msg: String, args: Any*) = log("[warn] ", msg, args)
    def error(msg: String, args: Any*) = log("[error] ", msg, args)
    
    def log(prefix: String, msg: String, args: Seq[Any]) = println(prefix + msg.format(args:_*))
}
