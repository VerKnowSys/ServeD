package com.verknowsys.served.utils.kqueue

import com.sun.jna._
import scala.actors.Actor
import scala.collection.mutable.{Map, ListBuffer}
import com.verknowsys.served.utils.monitor.Monitored

import com.verknowsys.served.utils.{CLibrary, kevent}

// TODO: throw custom exception, (or java.io something) instead of just Exception
// TODO: Handle removed files

@deprecated("Use com.verknowsys.served.fileevents package")
case class FileEvent(val evflags: Int)

/**
 * File watcher class. Holds file path, watch flags and event function
 *
 * @author teamon 
 */
 
@deprecated("Use com.verknowsys.served.fileevents package")
class KqueueWatcher(val kqueue: Kqueue, val path: String, val flags: Int)(f: => Unit) extends Actor with Monitored {
    case object StopWatching
    
    if(!(new java.io.File(path)).exists()) throw new java.io.FileNotFoundException(path)

    kqueue.register(this)
    start

    def act {
        loop {
            react {
                case FileEvent(evflags) if((flags & evflags) > 0) => f
                case FileEvent(evflags) => // dont care about that
                case StopWatching =>
                    kqueue.remove(this)
                    exit
                    
                case x: Any => println("[ERROR] ??? " + x.toString)
            }
        }
    }

    def stop {
        this ! StopWatching
    }
    
    override def toString = "KqueueWatcher(" + path + ")"
}

/**
 * Kqueue handler
 *
 * @author Author 
 */
@deprecated("Use com.verknowsys.served.fileevents package")
class Kqueue extends Thread {
    class KqueueException extends Exception
    class KeventException extends Exception
    class OpenException extends Exception
    
    setName("Kqueue")

    val kq = Kqueue.clib.kqueue()
    if(kq == -1) {
        throw new KqueueException
    }

    // Map(ident, (path, watchers))
    val idents = Map[Int, (String, ListBuffer[KqueueWatcher])]()

    start

    override def run {
        while(true) {
            val event = new kevent
            val nev = Kqueue.clib.kevent(kq, null, 0, event, 1, null)
            if(nev == -1){
                throw new KeventException
            } else if(nev > 0){
                idents.get(event.ident.intValue).foreach { _._2 foreach { _ ! FileEvent(event.fflags) } }
            }
        }
    }

    def register(watcher: KqueueWatcher) = synchronized {
        // check for path
        idents.find { case(ident, (path, watchers)) => path == watcher.path } match {
            case Some((ident, (path, list))) => 
                list += watcher // use existing file descriptor

            case None =>
                // open new file descriptor
                val ident = Kqueue.clib.open(watcher.path, CLibrary.O_RDONLY)
                if(ident == -1){
                    throw new OpenException
                }

                val event = new kevent(new NativeLong(ident),
                                    (CLibrary.EVFILT_VNODE).toShort,
                                    (CLibrary.EV_ADD | CLibrary.EV_ENABLE | CLibrary.EV_CLEAR).toShort,
                                    watcher.flags,
                                    new NativeLong(),
                                    null)

                val nev = Kqueue.clib.kevent(kq, event, 1, null, 0, null)
                if(nev == -1){
                    throw new KeventException
                }

                val list = new ListBuffer[KqueueWatcher]()
                list += watcher
                idents(ident) = (watcher.path, list)
        }
    }

    def remove(watcher: KqueueWatcher) = synchronized {
        idents.find { case(ident, (path, watchers)) => path == watcher.path } match {
            case Some((ident, (path, list))) => 
                list -= watcher
                if(list.isEmpty) {
                    // no more watchers, remove from map and close file
                    idents.remove(ident)
                    Kqueue.clib.close(ident)
                }

            case None => // TODO: Handle error (watcher not registered in list, should never happen)
        }
    }

}


/**
 * Kqueue main object
 * 
 * @example
 *     val watch = Kqueue.watch("/path/to/file", modified = true) { println("File modified") }
 *
 * @author teamon 
 */
@deprecated("Use com.verknowsys.served.fileevents package")
object Kqueue {
    protected val clib = CLibrary.instance
    protected val kq = new Kqueue

    /**
     * Setup watcher for specified path
     * 
     * @author teamon 
     */
    def watch(path: String, modified: Boolean = false, 
                            deleted: Boolean = false,
                            renamed: Boolean = false,
                            attributes: Boolean = false)(f: => Unit): KqueueWatcher = {

        import CLibrary._

        var flags = 0

        if(modified)    flags |= NOTE_WRITE | NOTE_EXTEND
        if(deleted)     flags |= NOTE_DELETE
        if(attributes)  flags |= NOTE_ATTRIB
        if(renamed)     flags |= NOTE_RENAME

        watch(path, flags)(f)
    }

    def watch(path: String, flags: Int)(f: => Unit): KqueueWatcher = new KqueueWatcher(kq, path, flags)(f)

}
