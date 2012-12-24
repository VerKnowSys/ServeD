package com.verknowsys.served.utils


import akka.actor.Actor


/**
 *  @author dmilith
 *
 *   This trait should be used by all ServeD actors
 */
trait SvdActor extends Actor with Logging with SvdUtils {

    val ancestor = this.getClass.getName
    val predecestor = super.getClass.getName
    // 2011-01-30 01:36:28 - dmilith - NOTE: txmt protocol example: txmt://open/?url=file://~/.bash_profile&line=11&column=2

//     /**
//      *  @author dmilith
//      *
//      *
//      */
//     def preRestart(reason: Throwable) = {
//         super.preRestart(reason, None) // XXX: CHECKME
//         log.trace("preRestart executed in %s".format(this.getClass))
//         log.error(
//             """
// Restarting Actor: (%s) cause of %s.
// Throwable details: (%s).
//             """.format(
//                 this.getClass.getName, reason.getMessage,
//                 reason.getStackTrace.map {
//                     traceElement =>
//                         """
//     url             - %s
//     class name      - %s
//     method name     - %s
//     file name       - %s:%s
//     native method   - %s
//                         """.format(
//                                 if (traceElement.getFileName.contains("Svd")) // 2011-01-30 03:34:13 - dmilith - NOTE: all project files will include Svd prefix
//                                     "txmt://open/?url=file://%s&line=%s".format(
//                                         findFileInDir(traceElement.getFileName),
//                                         traceElement.getLineNumber
//                                     )
//                                 else "No source",
//                                 traceElement.getClassName,
//                                 traceElement.getMethodName,
//                                 traceElement.getFileName, traceElement.getLineNumber,
//                                 traceElement.isNativeMethod
//                             )
//                 }.mkString
//             )
//         )

//     }


//     override def postStop = {
//         super.postStop
//         log.trace("postStop executed in %s".format(this.getClass))
//     }

    override def unhandled(msg: Any){
        super.unhandled(msg)
        log.warn("Message sent to %s was not recognized: %s", this.getClass, msg)
    }


    def traceReceive(f: Receive): Receive = {
        case msg =>
            // log.trace("Got message: %s", msg)
            f(msg)
    }

}
