package com.verknowsys.served.utils.fileevents

import com.sun.jna.NativeLong
import scala.collection.mutable.{Map, ListBuffer}
import akka.actor.{Actor, ActorRef}
import akka.actor.Actor.actorOf
import com.verknowsys.served.utils.kqueue.{CLibrary, kevent}
import com.verknowsys.served.utils._


case class KqueueFileEvent(ident: Int, flags: Int)
case class BareFileEvent(path: String, flags: Int)
case class FileEvent(path: String, flags: Int)

case class RegisterFileEvent(path: String, flags: Int)

class KqueueException extends Exception
class KeventException extends Exception
class FileOpenException extends Exception


class FileWatcher(val owner: ActorRef, val path: String, val flags: Int) extends Actor with Logged {
    trace("Starting new FileWatcher for % with % and %" % (owner, path, flags))
    
    def receive = {
        case BareFileEvent(path, evflags) if ((evflags & flags) > 0) => owner ! FileEvent(path, evflags) 
        // TODO: Create some case classes and send FileModified, FileCreated, FileDeleted etc
    }
}

trait FileEventsReactor {
    self: Actor =>
    
    def registerFileEventFor(path: String, flags: Int){
        Actor.registry.actorsFor[FileEventsManager].foreach { _ ! RegisterFileEvent(path, flags) }
    }
}


/** 
 * Main file events manager actor
 * 
 * 
 * Usage
 * {{{
 * class A extends Actor {
 *     Actor.registry.actorsFor[FileEventsManager].foreach { _ ! RegisterFileEvent("/path/to/file", flags) }
 * 
 *     def receive = {
 *         case FileEvent(path, flags) => println("New file event for for " + path)
 *     }
 * }
 * }}}
 * 
 * or by mixing in FileEventsReactor trait
 * {{{
 * class A extends Actor with FileEventsReactor {
 *     registerFileEventFor("/path/to/file", flags)
 * 
 *     def receive = {
 *         case FileEvent(path, flags) => println("New file event for for " + path)
 *     }
 * }
 * }}}
 * 
 * @author teamon
 */
class FileEventsManager extends Actor with Logged {
    trace("Starting FileEventsManager")
    
    protected val clib = CLibrary.instance
    protected val kq = clib.kqueue() // NOTE: C call
    
    // check kqueue
    if(kq == -1){
        throw new KqueueException
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
        case RegisterFileEvent(path, flags) =>
            idents.find { case(_, (p, _)) => p == path } match {
                case Some((_, (p, list))) =>
                    self.sender.foreach {
                        list += spawnNewFileWatcher(_, path, flags)
                    }

                    
                case None =>
                    self.sender.foreach { registerNewFileEvent(_, path, flags) }
            }
            
        // 
        case KqueueFileEvent(ident, flags) => 
            idents.get(ident.intValue).foreach { 
                case (path, list) => list foreach { _ ! BareFileEvent(path, flags) }
            }
    }
    
    
    protected def registerNewFileEvent(owner: ActorRef, path: String, flags: Int) = synchronized {
        val ident = clib.open(path, CLibrary.O_RDONLY)
        if(ident == -1){
            throw new FileOpenException
        }
        
        val event = new kevent(new NativeLong(ident),
                    (CLibrary.EVFILT_VNODE).toShort,
                    (CLibrary.EV_ADD | CLibrary.EV_ENABLE | CLibrary.EV_CLEAR).toShort,
                    flags,
                    new NativeLong(),
                    null)
        
        val nev = clib.kevent(kq, event, 1, null, 0, null)
        if(nev == -1){
            throw new KeventException
        }

        idents(ident) = (path, ListBuffer[ActorRef](spawnNewFileWatcher(owner, path, flags)))
    }
    
    protected def spawnNewFileWatcher(owner: ActorRef, path: String, flags: Int) = {
        val watcher = actorOf(new FileWatcher(owner, path, flags))
        self.link(watcher)
        watcher.start
        watcher
    }
}
