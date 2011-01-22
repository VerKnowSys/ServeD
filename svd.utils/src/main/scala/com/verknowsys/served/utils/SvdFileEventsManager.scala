package com.verknowsys.served.utils

import com.sun.jna.NativeLong
import scala.collection.mutable.{Map, ListBuffer}

import akka.actor.{Actor, ActorRef}
import akka.actor.Actor.actorOf
import akka.util.Logging

import com.verknowsys.served.utils.signals.{Success, Failure}

object events {
    case class SvdKqueueFileEvent(ident: Int, flags: Int)
    case class BareFileEvent(path: String, flags: Int)
    case class FileEvent(path: String, flags: Int)
    case class RegisterFileEvent(path: String, flags: Int)

    class SvdKqueueException extends Exception
    class KeventException extends Exception
    class FileOpenException extends Exception
}

import events._

/** 
 * File watcher actor. It receives kqueue events and sends message to [owner] if flags match 
 * 
 * @param owner Owner actor of this file watch
 * @param path File path to watch
 * @param flags kqueue flags to watch
 * @author teamon
 */
class SvdFileWatcher(val owner: ActorRef, val path: String, val flags: Int) extends Actor with Logging {
    log.trace("Starting new SvdFileWatcher for %s with %s and %s", owner, path, flags)
    
    def receive = {
        case BareFileEvent(path, evflags) if ((evflags & flags) > 0) => owner ! FileEvent(path, evflags) 
        case _ => 
        // case BareFileEvent(path, evflags) =>
        // TODO: Create some case classes and send FileModified, FileCreated, FileDeleted etc
    }
}



/** 
 * Include this trait in any akka Actor
 *
 * Usage:
 * {{{
 * class MyActor extends Actor with SvdFileEventsReactor {
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
trait SvdFileEventsReactor {
    self: Actor =>
    
    final val Modified = SvdCLibrary.NOTE_WRITE | SvdCLibrary.NOTE_EXTEND
    final val Deleted  = SvdCLibrary.NOTE_DELETE
    final val Renamed  = SvdCLibrary.NOTE_RENAME
    final val AttributesChanged = SvdCLibrary.NOTE_ATTRIB
    
    def registerFileEventFor(path: String, flags: Int){
        Actor.registry.actorsFor[SvdFileEventsManager].foreach { _ ! RegisterFileEvent(path, flags) }
    }
}


/** 
 * Main file events manager actor
 * 
 * For internal use only. STart it using {{{ actorOf[FileEventsSvdManager] }}}
 * 
 * @author teamon
 */
class SvdFileEventsManager extends Actor with Logging {
    import SvdCLibrary._
    
    log.trace("Starting SvdFileEventsManager")
    
    protected val clib = SvdCLibrary.instance
    protected val kq = clib.kqueue() // NOTE: C call
    
    // check kqueue
    if(kq == -1){
        throw new SvdKqueueException
    }
    
    /** 
     * Mutable map holding [file descriptor => (path, file watcher actor)] 
     * 
     * @author teamon
     */
    protected val idents = Map[Int, (String, ListBuffer[ActorRef])]()
        
    /** 
     * BSD kqueue events reader thread 
     * 
     * @author teamon
     */
    protected val readerThread = new Thread {
        override def run {
            while(true){
                val event = new SvdKevent
                val nev = clib.SvdKevent(kq, null, 0, event, 1, null)

                if(nev > 0){
                    SvdFileEventsManager.this.self ! SvdKqueueFileEvent(event.ident.intValue, event.fflags)
                } else if(nev == -1){
                    throw new KeventException
                }
            }
        }
    }
    readerThread.start
        
    
    def receive = {
        // register new file event, sent from any actor
        case RegisterFileEvent(path, flags) =>
            idents.find { case(_, (p, _)) => p == path } match {
                case Some((_, (p, list))) =>
                    self.sender.foreach {
                        list += spawnNewSvdFileWatcher(_, path, flags)
                    }

                case None =>
                    self.sender.foreach { registerNewFileEvent(_, path, flags) }
            }
            self reply Success

        // Forward event sent by kqueue to file watchers
        case SvdKqueueFileEvent(ident, flags) => 
            idents.get(ident.intValue).foreach { 
                case (path, list) => list foreach { _ ! BareFileEvent(path, flags) }
            }
    }
    
    
    protected def registerNewFileEvent(owner: ActorRef, path: String, flags: Int) = synchronized {
        val ident = clib.open(path, O_RDONLY)
        if(ident == -1){
            throw new FileOpenException
        }
        
        val event = new SvdKevent(new NativeLong(ident),
                    (EVFILT_VNODE).toShort,
                    (EV_ADD | EV_ENABLE | EV_CLEAR).toShort,
                    flags,
                    new NativeLong(),
                    null)
        
        val nev = clib.SvdKevent(kq, event, 1, null, 0, null)
        if(nev == -1){
            throw new KeventException
        }

        idents(ident) = (path, ListBuffer[ActorRef](spawnNewSvdFileWatcher(owner, path, flags)))
    }
    
    protected def spawnNewSvdFileWatcher(owner: ActorRef, path: String, flags: Int) = {
        val watcher = actorOf(new SvdFileWatcher(owner, path, flags))
        self.link(watcher)
        watcher.start
        watcher
    }
}
