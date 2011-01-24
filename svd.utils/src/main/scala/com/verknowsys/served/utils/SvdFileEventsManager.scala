package com.verknowsys.served.utils

import com.sun.jna.NativeLong
import scala.collection.mutable.{Map, ListBuffer}

import akka.actor.{Actor, ActorRef}
import akka.actor.Actor.actorOf
import akka.util.Logging

import com.verknowsys.served.utils.signals.{Success, Failure}
import events._


object events {
    case class SvdKqueueFileEvent(ident: Int, flags: Int)
    case class SvdFileEvent(path: String, flags: Int)
    case class SvdRegisterFileEvent(path: String, flags: Int, ref: ActorRef)
    case class SvdUnregisterFileEvent(ref: ActorRef)

    class SvdKqueueException extends Exception
    class SvdKeventException extends Exception
    class SvdFileOpenException extends Exception
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
trait SvdFileEventsReactor {
    self: Actor with Logging =>
    
    final val Modified          = CLibrary.NOTE_WRITE | CLibrary.NOTE_EXTEND
    final val Deleted           = CLibrary.NOTE_DELETE
    final val Renamed           = CLibrary.NOTE_RENAME
    final val AttributesChanged = CLibrary.NOTE_ATTRIB
    
    def registerFileEventFor(path: String, flags: Int){
        Actor.registry.actorFor[SvdFileEventsManager] match {
            case Some(fem) => fem ! SvdRegisterFileEvent(path, flags, this.self)
            case None => log.error("Could not register file watcher. FileEventsManager worker not found.")
        }
    }
    
    def unregisterFileEvents {
        Actor.registry.actorFor[SvdFileEventsManager] match {
            case Some(fem) => fem ! SvdUnregisterFileEvent(this.self)
            case None => log.error("Could not unregister file watcher. FileEventsManager worker not found.")
        }
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
class SvdFileEventsManager extends Actor with Logging {
    import CLibrary._
    
    log.info("SvdFileEventsManager is loading")
    
    protected val clib = CLibrary.instance
    protected val kq = clib.kqueue() // NOTE: C call
    
    // check kqueue
    if(kq == -1){
        throw new SvdKqueueException
    }
    
    /** 
     * Mutable map holding [file descriptor => (path, list of (flags, actor ref))] 
     * 
     * @author teamon
     */
    protected val idents = Map[Int, (String, ListBuffer[(Int, ActorRef)])]()
        
    /** 
     * BSD kqueue events reader thread 
     * 
     * @author teamon
     */
    protected val readerThread = new Thread {
        override def run {
            while(true){
                val event = new kevent
                val nev = clib.kevent(kq, null, 0, event, 1, null)

                if(nev > 0){
                    SvdFileEventsManager.this.self ! SvdKqueueFileEvent(event.ident.intValue, event.fflags)
                } else if(nev == -1){
                    throw new SvdKeventException
                }
            }
        }
    }
    readerThread.start
        
    
    def receive = {
        // register new file event, sent from any actor
        case SvdRegisterFileEvent(path, flags, ref) =>
            idents.find { case(i, (p, l)) => p == path } match {
                case Some((_, (p, list))) => list += ((flags, ref))
                case None => registerNewFileEvent(path, flags, ref)
            }
            
            log.trace("Registered new file event: %s / %s for %s", path, flags, ref)
            log.trace("Registered file events: %s", idents)
            self reply Success
            
        case SvdUnregisterFileEvent(ref) =>
            idents.foreach { case ((ident, (path, list))) =>
                list filter { case ((flags, actor)) => actor == ref } foreach { list -= _ }
                if(list.isEmpty){
                    idents -= ident
                }
            }
            
            log.trace("Unregistered file events for %s", ref)
            log.trace("Registered file events: %s", idents)
            // self reply Success

        // Forward event sent by kqueue to file watchers
        case SvdKqueueFileEvent(ident, flags) => 
            log.trace("New kqueue file event. flags: %x", flags)
        
            idents.get(ident.intValue).foreach { 
                case (path, list) => list collect {
                    case ((fl, ref)) if (fl & flags) > 0 => ref ! SvdFileEvent(path, flags)
                }
            }
    }
    
    
    protected def registerNewFileEvent(path: String, flags: Int, ref: ActorRef) = synchronized {
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
        if(nev == -1){
            throw new SvdKeventException
        }

        idents(ident) = (path, ListBuffer[(Int, ActorRef)]((flags, ref)))
    }
}
