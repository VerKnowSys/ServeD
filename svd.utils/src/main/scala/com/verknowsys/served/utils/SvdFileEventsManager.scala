package com.verknowsys.served.utils


import scala.collection.mutable.{HashMap => MutableMap, ListBuffer}
import com.sun.jna.NativeLong
import Events._
import akka.actor._
import com.verknowsys.served.api._
import com.verknowsys.served._


object Events {
    abstract class Base extends ApiMessage
    case class SvdKqueueFileEvent(ident: Int, flags: Int) extends Base
    case class SvdFileEvent(path: String, flags: Int) extends Base
    case class SvdRegisterFileEvent(path: String, flags: Int, ref: ActorRef) extends Base
    case class SvdUnregisterFileEvent(ref: ActorRef) extends Base

    class SvdKqueueException extends Exception
    class SvdKeventException extends Exception
    class SvdFileOpenException extends Exception

    final val Modified          = CLibrary.NOTE_WRITE | CLibrary.NOTE_EXTEND
    final val Deleted           = CLibrary.NOTE_DELETE
    final val Renamed           = CLibrary.NOTE_RENAME
    final val AttributesChanged = CLibrary.NOTE_ATTRIB
    final val Revoked           = CLibrary.NOTE_REVOKE
    final val Linked            = CLibrary.NOTE_LINK
    final val All               = CLibrary.NOTE_WRITE | CLibrary.NOTE_EXTEND | CLibrary.NOTE_DELETE | CLibrary.NOTE_RENAME | CLibrary.NOTE_ATTRIB | CLibrary.NOTE_REVOKE | CLibrary.NOTE_LINK

    final val EvError           = CLibrary.EV_ERROR
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
 * @author dmilith
 *
 */
trait SvdFileEventsReactor extends SvdActor with Logging with SvdUtils {

    def registerFileEventFor(path: String, flags: Int, ref: ActorRef = self, uid: Int = 0) {
        import java.io.File

        def bindEvent = self ! SvdRegisterFileEvent(path, flags, ref)

        val file = new File(path)
        if (file.exists) {
            log.debug("Creating watch on existing file: %s.", path)
            bindEvent
        } else {
            log.debug("File to watch doesn't exists. Assumming that we want to monitor a folder: %s", file)
            file.mkdirs
            log.info("Creating and starting monitoring of new folder: %s", file)
            bindEvent
        }
    }


    def unregisterFileEvents(ref: ActorRef = self) {
        val fem = context.actorFor("akka://%s@%s:%d/user/SvdFileEventsManager".format(SvdConfig.served, SvdConfig.remoteApiServerHost, SvdConfig.remoteApiServerPort))
        fem ! SvdUnregisterFileEvent(ref)
        log.debug("Unregistering events for Account Manager: %s", ref)
    }


    override def preRestart(reason: Throwable, message: Option[Any]) {
        log.warn("preRestart caused by reason: %s with message: %s", reason, message)
        unregisterFileEvents()
        super.preRestart(reason, message)
    }


    override def postStop {
        log.debug("Post Stop in SvdFileEventsManager")
        unregisterFileEvents()
        super.postStop
    }
}



/**
 * Main file events manager actor
 *
 * For internal use only. Start it using {{{ actorOf[SvdFileEventsManager] }}}
 *
 * @author teamon
 */
class SvdFileEventsManager extends Actor with Logging with SvdActor with SvdUtils {
    import CLibrary._
    import Events._

    log.info("SvdFileEventsManager is loading")

    protected lazy val kq = {
        val k = clib.kqueue() // NOTE: C call
        if(k == -1) throwException[SvdKqueueException]("kQueue system call failed!") // check kqueue
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
    protected lazy val readerThread = loopThread {
        if(self != null){
            val event = new kevent
            val nev = clib.kevent(kq, null, 0, event, 1, null)

            if(nev > 0 && event != null && self != null){
                self ! SvdKqueueFileEvent(event.ident.intValue, event.fflags)
            } else if(nev == -1){
                throwException[SvdKeventException]("kEvent exception!")
            }
        }
    }

    override def preStart {
        super.preStart
        readerThread.start
        log.info("SvdFileEventsManager initialized")
    }

    override def preRestart(reason: Throwable, message: Option[Any]) {
        readerThread.kill
    }

    override def postStop {
        log.debug("PostStop in FileEventsManager")
        readerThread.kill
    }


    def receive = {

        // register new file event, sent from any actor
        case SvdRegisterFileEvent(path, flags, ref) =>
            idents.find { case(i, (p, l)) => p == path } match {
                case Some((_, (p, list))) => list += ((flags, ref))
                case None => registerNewFileEvent(path, flags, ref)
            }

            log.debug("Registered new file event: %s / %s for %s", path, flags, ref)
            log.debug("Registered file events: %s", idents)
            sender ! Success

        case SvdUnregisterFileEvent(ref) =>
            idents.foreach { case ((ident, (path, list))) =>
                list filter { case ((flags, actor)) => actor == ref } foreach { list -= _ }
                if(list.isEmpty){
                    idents -= ident
                }
            }

            log.debug("Unregistered file events for %s", ref)
            log.debug("Registered file events: %s", idents)
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

        case Success =>
            log.trace("Success in SEM")


        // case SvdFileEvent(path, flags) =>
        //     log.warn("REACT on file event: %s, %s.".format(path, flags))

        // case x: SvdFileEvent =>
        //     log.trace("REACT on file event: %s.".format(x))

        // case x: events.SvdFileEvent =>
        //     sender forward x
        //     // log.trace("REACT on file event on path: %s. Flags: %s".format(path, flags))

        case x: Any =>
            log.warn("%s has received unknown signal: %s".format(this.getClass, x))
            // sender ! Error("Unknown signal: %s".format(x))

    }


    protected def registerNewFileEvent(path: String, flags: Int, ref: ActorRef): Unit = synchronized {
        val ident = clib.open(path, O_RDONLY)
        if(ident == -1){
            throwException[SvdFileOpenException]("Failed to register file event on %s with flags: %s!".format(path, flags))
        }

        val event = new kevent(new NativeLong(ident),
                    (EVFILT_VNODE).toShort,
                    (EV_ADD | EV_ENABLE | EV_CLEAR).toShort, // TODO: think about ONESHOT option with parametter
                    flags,
                    new NativeLong(),
                    null)

        val nev = clib.kevent(kq, event, 1, null, 0, null)
        if (nev == -1) {
            log.error("Failed to register kevent!")
            throwException[Exception]("Failed to register kevent!")
            // registerNewFileEvent(path, flags, ref) // XXX: try again, not really safe
            // throw new SvdKeventException
        } else {
            val list = new ActorRefList()
            list += ((flags, ref))
            idents(ident) = (path, list)
        }

    }
}
