package com.verknowsys.served.utils

import com.sun.jna.NativeLong
import scala.collection.mutable.{Map, ListBuffer}

import akka.actor.{Actor, ActorRef}
import akka.actor.Actor.actorOf
import akka.util.Logging

import com.verknowsys.served.utils.signals.{Success, Failure}

object events {
    case class KqueueFileEvent(ident: Int, flags: Int)
    case class FileEvent(path: String, flags: Int)
    case class RegisterFileEvent(path: String, flags: Int, ref: ActorRef)

    class KqueueException extends Exception
    class KeventException extends Exception
    class FileOpenException extends Exception
    
}

import events._

/** 
 * Include this trait in any akka Actor
 *
 * Usage:
 * {{{
 * class MyActor extends Actor with FileEventsReactor {
 *     registerFileEventFor("/path/to/file", Modified)
 * 
 *     def receive = {
 *         case FileEvent(path, Modified) => println("New file event for for " + path)
 *     }
 * }
 * }}}
 * 
 * Possible flags values: Modified, Deleted, Renamed, AttributesChanged
 * 
 * @author teamon
 */
trait FileEventsReactor {
    self: Actor with Logging =>
    
    final val Modified          = CLibrary.NOTE_WRITE | CLibrary.NOTE_EXTEND
    final val Deleted           = CLibrary.NOTE_DELETE
    final val Renamed           = CLibrary.NOTE_RENAME
    final val AttributesChanged = CLibrary.NOTE_ATTRIB
    
    def registerFileEventFor(path: String, flags: Int){
        Actor.registry.actorFor[FileEventsManager] match {
            case Some(fem) => fem ! RegisterFileEvent(path, flags, this.self)
            case None => log.error("Could not register file watcher. FileEventsManager worker not found.")
        }
    }
}


/** 
 * Main file events manager actor
 * 
 * For internal use only. STart it using {{{ actorOf[FileEventsManager] }}}
 * 
 * @author teamon
 */
class FileEventsManager extends Actor with Logging {
    import CLibrary._
    
    log.trace("Starting FileEventsManager")
    
    protected val clib = CLibrary.instance
    protected val kq = clib.kqueue() // NOTE: C call
    
    // check kqueue
    if(kq == -1){
        throw new KqueueException
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
                    FileEventsManager.this.self ! KqueueFileEvent(event.ident.intValue, event.fflags)
                } else if(nev == -1){
                    throw new KeventException
                }
            }
        }
    }
    readerThread.start
        
    
    def receive = {
        // register new file event, sent from any actor
        case RegisterFileEvent(path, flags, ref) =>
            idents.find { case(_, (p, _)) => p == path } match {
                case Some((_, (p, list))) => list += ((flags, ref))
                case None => registerNewFileEvent(path, flags, ref)
            }
            
            log.trace("Registered new file event: %s / %s for %s", path, flags, ref)
            self reply Success

        // Forward event sent by kqueue to file watchers
        case KqueueFileEvent(ident, flags) => 
            log.trace("New kqueue file event. flags: %x", flags)
        
            idents.get(ident.intValue).foreach { 
                case (path, list) => list collect {
                    case ((fl, ref)) if (fl & flags) > 0 => ref ! FileEvent(path, flags)
                }
            }
    }
    
    
    protected def registerNewFileEvent(path: String, flags: Int, ref: ActorRef) = synchronized {
        val ident = clib.open(path, O_RDONLY)
        if(ident == -1){
            throw new FileOpenException
        }
        
        val event = new kevent(new NativeLong(ident),
                    (EVFILT_VNODE).toShort,
                    (EV_ADD | EV_ENABLE | EV_CLEAR).toShort,
                    flags,
                    new NativeLong(),
                    null)
        
        val nev = clib.kevent(kq, event, 1, null, 0, null)
        if(nev == -1){
            throw new KeventException
        }

        idents(ident) = (path, ListBuffer[(Int, ActorRef)]((flags, ref)))
    }
}
