package com.verknowsys.served.utils.kqueue

import com.sun.jna._
import scala.actors.Actor
import scala.collection.mutable.{Map, ListBuffer}
import com.verknowsys.served.utils.monitor.SvdMonitored

import com.verknowsys.served.utils.{SvdCLibrary, kevent}

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
class SvdKqueueWatcher(val kqueue: SvdKqueue, val path: String, val flags: Int)(f: => Unit) extends Actor with SvdMonitored {
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
    
    override def toString = "SvdKqueueWatcher(" + path + ")"
}

/**
 * SvdKqueue handler
 *
 * @author Author 
 */
@deprecated("Use com.verknowsys.served.fileevents package")
class SvdKqueue extends Thread {
    class SvdKqueueException extends Exception
    class KeventException extends Exception
    class OpenException extends Exception
    
    setName("SvdKqueue")

    val kq = SvdKqueue.clib.kqueue()
    if(kq == -1) {
        throw new SvdKqueueException
    }

    // Map(ident, (path, watchers))
    val idents = Map[Int, (String, ListBuffer[SvdKqueueWatcher])]()

    start

    override def run {
        while(true) {
            val event = new kevent
            val nev = SvdKqueue.clib.kevent(kq, null, 0, event, 1, null)
            if(nev == -1){
                throw new KeventException
            } else if(nev > 0){
                idents.get(event.ident.intValue).foreach { _._2 foreach { _ ! FileEvent(event.fflags) } }
            }
        }
    }

    def register(watcher: SvdKqueueWatcher) = synchronized {
        // check for path
        idents.find { case(ident, (path, watchers)) => path == watcher.path } match {
            case Some((ident, (path, list))) => 
                list += watcher // use existing file descriptor

            case None =>
                // open new file descriptor
                val ident = SvdKqueue.clib.open(watcher.path, SvdCLibrary.O_RDONLY)
                if(ident == -1){
                    throw new OpenException
                }

                val event = new kevent(new NativeLong(ident),
                                    (SvdCLibrary.EVFILT_VNODE).toShort,
                                    (SvdCLibrary.EV_ADD | SvdCLibrary.EV_ENABLE | SvdCLibrary.EV_CLEAR).toShort,
                                    watcher.flags,
                                    new NativeLong(),
                                    null)

                val nev = SvdKqueue.clib.kevent(kq, event, 1, null, 0, null)
                if(nev == -1){
                    throw new KeventException
                }

                val list = new ListBuffer[SvdKqueueWatcher]()
                list += watcher
                idents(ident) = (watcher.path, list)
        }
    }

    def remove(watcher: SvdKqueueWatcher) = synchronized {
        idents.find { case(ident, (path, watchers)) => path == watcher.path } match {
            case Some((ident, (path, list))) => 
                list -= watcher
                if(list.isEmpty) {
                    // no more watchers, remove from map and close file
                    idents.remove(ident)
                    SvdKqueue.clib.close(ident)
                }

            case None => // TODO: Handle error (watcher not registered in list, should never happen)
        }
    }

}


/**
 * SvdKqueue main object
 * 
 * @example
 *     val watch = SvdKqueue.watch("/path/to/file", modified = true) { println("File modified") }
 *
 * @author teamon 
 */
@deprecated("Use com.verknowsys.served.fileevents package")
object SvdKqueue {
    protected val clib = SvdCLibrary.instance
    protected val kq = new SvdKqueue

    /**
     * Setup watcher for specified path
     * 
     * @author teamon 
     */
    def watch(path: String, modified: Boolean = false, 
                            deleted: Boolean = false,
                            renamed: Boolean = false,
                            attributes: Boolean = false)(f: => Unit): SvdKqueueWatcher = {

        import SvdCLibrary._

        var flags = 0

        if(modified)    flags |= NOTE_WRITE | NOTE_EXTEND
        if(deleted)     flags |= NOTE_DELETE
        if(attributes)  flags |= NOTE_ATTRIB
        if(renamed)     flags |= NOTE_RENAME

        watch(path, flags)(f)
    }

    def watch(path: String, flags: Int)(f: => Unit): SvdKqueueWatcher = new SvdKqueueWatcher(kq, path, flags)(f)

}
