package com.verknowsys.served


import com.verknowsys.served._
import com.verknowsys.served.api.ApiMessage
import akka.dispatch._
import akka.pattern.ask
import akka.remote._
import akka.util.Duration
import akka.util.Timeout
import akka.util.duration._
import akka.actor._


package object cli {

    type Args = Iterable[String]
    type Svd = akka.actor.ActorRef


    // def request(msg: ApiMessage, context: ActorContext)(f: PartialFunction[Any, Unit])(implicit svd: Svd) {
    //     (svd ? msg) onSuccess {
    //         case Some(response) if f.isDefinedAt(response) => f(response)
    //         case Some(response) => error("Unhandled reponse %s", response)
    //         case None => error("Connection timeout")

    //     } onFailure {
    //         case x =>
    //             error("Error on request: %s".format(x))

    //     }
    // }

    def confirm(msg: String) = {
        warn(msg)
        Console.readLine("[Y/n]: ") == "Y"
    }

    def quit {
        // system.shutdown
        sys.exit(0)
    }

    def print(list: Iterable[String]) = list.foreach(println)

    def info(msg: String, args: Any*) = log("", msg, args)
    def warn(msg: String, args: Any*) = log("[warn] ", msg, args)
    def error(msg: String, args: Any*) = log("[error] ", msg, args)

    def log(prefix: String, msg: String, args: Seq[Any]) = println(prefix + msg.format(args:_*))
}
