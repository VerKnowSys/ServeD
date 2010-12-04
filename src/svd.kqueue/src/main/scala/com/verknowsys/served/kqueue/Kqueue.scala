package com.verknowsys.served.kqueue

import com.sun.jna._
import scala.actors.Actor
import scala.collection.mutable.{Map, ListBuffer}

// TODO: throw custom exception, (or java.io something) instead of just Exception
// TODO: Handle removed files

case class FileEvent(val evflags: Int)

protected class KqueueWatcher(val kqueue: Kqueue, val path: String, val flags: Int)(f: => Unit) extends Actor {
    case object StopWatching

    var keep = true

    kqueue.register(this)
    start

    def act {
        while(keep) {
            react {
                case FileEvent(evflags) if((flags & evflags) > 0) => f
                case StopWatching =>
                    kqueue.remove(this)
                    keep = false
            }
        }
    }

    def stop {
        this ! StopWatching
    }
}

protected class Kqueue extends Actor {
    val kq = Kqueue.clib.kqueue()
    if(kq == -1) {
        // TODO: Handle error
        throw new Exception("kqueue() error")
        println("kqueue() error")
    }

    // Map(ident, (path, watchers))
    val idents = Map[Int, (String, ListBuffer[KqueueWatcher])]()

    start

    def act {
        loop {
            val event = new kevent
            val nev = Kqueue.clib.kevent(kq, null, 0, event, 1, null)
            if(nev == -1){
                // TODO: Handle error
                throw new Exception("kevent() read error")
                println("kevent() error")
            } else if(nev > 0){
                idents.get(event.ident.intValue).foreach { _._2 foreach { _ ! FileEvent(event.fflags) } }
            }
        }
    }

    def register(watcher: KqueueWatcher){
        // check for path
        idents.find { case(ident, (path, watchers)) => path == watcher.path } match {
            case Some((ident, (path, list))) => 
                list += watcher // use existing file descriptor

            case None =>
                // open new file descriptor
                val ident = Kqueue.clib.open(watcher.path, CLibrary.O_RDONLY)
                if(ident == -1){
                    // TODO: Handle error
                    throw new Exception("open() error")
                    return
                }

                val event = new kevent(new NativeLong(ident),
                                    (CLibrary.EVFILT_VNODE).toShort,
                                    (CLibrary.EV_ADD | CLibrary.EV_ENABLE | CLibrary.EV_CLEAR).toShort,
                                    watcher.flags,
                                    new NativeLong(),
                                    null)

                val nev = Kqueue.clib.kevent(kq, event, 1, null, 0, null)
                if(nev == -1){
                    // TODO: Handle error
                    throw new Exception("kevent() register error")
                    return
                }

                val list = new ListBuffer[KqueueWatcher]()
                list += watcher
                idents(ident) = (watcher.path, list)
        }
    }

    def remove(watcher: KqueueWatcher){
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
 *
 * touch /tmp/aa        -> attributes
 * echo "x" > /tmp/aa   -> modified & attributes
 * echo "a" >> tmp/aa   -> modified
 * mv /tmp/aa /tmp/bb   -> renamed
 * rm /tmp/bb           -> deleted 
 *
 */
object Kqueue {
    val clib = CLibrary.instance
    val kq = new Kqueue

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
