package com.verknowsys.served.utils


import scala.collection.mutable.{HashMap => MutableMap, ListBuffer}
import akka.dispatch._
import akka.pattern.ask
import akka.remote._
import akka.util.Duration
import akka.util.Timeout
import akka.util.duration._
import akka.actor._
import com.sun.jna.NativeLong
import com.sun.jna.{Native, Library}
import events._

import events._
import com.verknowsys.served.utils.events._
import java.io._
import org.apache.commons.io.FileUtils
import events._
import akka.actor._
import akka.actor.Actor._
import com.verknowsys.served.api._
import com.verknowsys.served.api.git._
import com.verknowsys.served._


object events {
    case class SvdKqueueFileEvent(ident: Int, flags: Int)
    case class SvdFileEvent(path: String, flags: Int)
    case class SvdRegisterFileEvent(path: String, flags: Int, ref: ActorRef)
    case class SvdUnregisterFileEvent(ref: ActorRef)

    class SvdKqueueException extends Exception
    class SvdKeventException extends Exception
    class SvdFileOpenException extends Exception

    final val Modified          = CLibrary.NOTE_WRITE | CLibrary.NOTE_EXTEND
    final val Deleted           = CLibrary.NOTE_DELETE
    final val Renamed           = CLibrary.NOTE_RENAME
    final val AttributesChanged = CLibrary.NOTE_ATTRIB
}


/**
 * Include this trait in your actor to use file events
 *
 * {{{
 * class MyActor extends Actor with FileEventsReactor {
 *     def receive = {
 *         case FileEvent(path, flags) => // handle event ...
 *     }
 *
 *     override def preStart {
 *         registerFileEventFor("/path/to/file", Modified)
 *     }
 *
 *     override def preRestart(reason: Throwable) {
 *         super.preRestart(reason) // Must put this line to unregistr events on restart
 *     }
 *
 *     override def postStop {
 *         super.postStop // Must put this line to unregistr events on stop
 *     }
 *
 * }
 * }}}
 *
 * @author teamon
 */
trait SvdFileEventsReactor extends SvdExceptionHandler {
    self: Actor with Logging =>

    def registerFileEventFor(path: String, flags: Int){
        val fem = context.actorFor("akka://%s@127.0.0.1:%d/user/SvdFileEventsManager".format(SvdConfig.served, SvdConfig.remoteApiServerPort)) // , name = "SvdFileEventsManager"
        fem ! SvdRegisterFileEvent(path, flags, fem)

        // val res = Await.result(future, timeout.duration).asInstanceOf[ActorRef]
        // XXX: CHECKME
        // res match {
        //     case Some(fem) => fem ! SvdRegisterFileEvent(path, flags, this.self)
        //     case None => log.warn("Could not register file event. FileEventsManager worker not found.")
        // }
    }

    def unregisterFileEvents {
        val fem = context.actorOf(Props[SvdFileEventsManager])
        fem ! SvdUnregisterFileEvent(fem)
        // XXX: CHECKME
        super.postStop // 2011-01-30 01:06:54 - dmilith - NOTE: execute SvdExceptionHandler's code
    }

    override def preRestart(reason: Throwable) = unregisterFileEvents
    override def postStop = unregisterFileEvents
}



/**
 * Main file events manager actor
 *
 * For internal use only. Start it using {{{ actorOf[SvdFileEventsManager] }}}
 *
 * @author teamon
 */
class SvdFileEventsManager extends Actor with Logging with SvdExceptionHandler {
    import CLibrary._

    log.info("SvdFileEventsManager is loading")

    protected val clib = CLibrary.instance
    protected lazy val kq = {
        val k = clib.kqueue() // NOTE: C call
        if(k == -1) throw new SvdKqueueException // check kqueue
        k
    }

    type ActorRefList = ListBuffer[(Int, ActorRef)]
    type IdentsMap = MutableMap[Int, (String, ActorRefList)]

    /**
     * Mutable map holding [file descriptor => (path, list of (flags, actor ref))]
     *
     * @author teamon
     */
    protected val idents = new IdentsMap

    /**
     * BSD kqueue events reader thread
     *
     * @author teamon
     */
    protected lazy val readerThread = SvdUtils.loopThread {
        if(self != null){
            val event = new kevent
            val nev = clib.kevent(kq, null, 0, event, 1, null)

            if(nev > 0 && event != null && self != null){
                self ! SvdKqueueFileEvent(event.ident.intValue, event.fflags)
            } else if(nev == -1){
                throw new SvdKeventException // TODO: Catch this somehow
            }
        }
    }

    override def preStart {
        readerThread.start
    }

    override def preRestart(reason: Throwable) {
        readerThread.kill
    }

    override def postStop {
        readerThread.kill
    }


    def receive = {

        case Init =>
            log.debug("SvdFileEventsManager initialized")
            sender ! Success


        // register new file event, sent from any actor
        case SvdRegisterFileEvent(path, flags, ref) =>
            idents.find { case(i, (p, l)) => p == path } match {
                case Some((_, (p, list))) => list += ((flags, ref))
                case None => registerNewFileEvent(path, flags, ref)
            }

            log.trace("Registered new file event: %s / %s for %s", path, flags, ref)
            log.trace("Registered file events: %s", idents)
            sender ! Success

        case SvdUnregisterFileEvent(ref) =>
            idents.foreach { case ((ident, (path, list))) =>
                list filter { case ((flags, actor)) => actor == ref } foreach { list -= _ }
                if(list.isEmpty){
                    idents -= ident
                }
            }

            log.trace("Unregistered file events for %s", ref)
            log.trace("Registered file events: %s", idents)
            sender ! Success

        // Forward event sent by kqueue to file watchers
        case SvdKqueueFileEvent(ident, flags) =>
            log.trace("New kqueue file event. flags: %x", flags)

            idents.get(ident.intValue).foreach {
                case (path, list) => list collect {
                    case ((fl, ref)) if (fl & flags) > 0 => ref ! SvdFileEvent(path, flags)
                }
            }
            sender ! Success

        case x: Any =>
            log.warn("%s has received unknown signal: %s".format(this.getClass, x))
            sender ! Error("Unknown signal: %s".format(x))

    }


    protected def registerNewFileEvent(path: String, flags: Int, ref: ActorRef): Unit = synchronized {
        val ident = clib.open(path, O_RDONLY)
        if(ident == -1){
            throw new SvdFileOpenException
        }

        val event = new kevent(new NativeLong(ident),
                    (EVFILT_VNODE).toShort,
                    (EV_ADD | EV_ENABLE | EV_CLEAR).toShort,
                    flags,
                    new NativeLong(),
                    null)

        val nev = clib.kevent(kq, event, 1, null, 0, null)
        if (nev == -1) {
            log.error("Failed to register kevent!")
            SvdUtils.throwException[Exception]("Failed to register kevent!")
            // registerNewFileEvent(path, flags, ref) // XXX: try again, not really safe
            // throw new SvdKeventException
        } else {
            val list = new ActorRefList()
            list += ((flags, ref))
            idents(ident) = (path, list)
        }

    }
}
