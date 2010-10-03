package com.verknowsys.served.utils

import net.contentobjects.jnotify._

// See http://jnotify.sourceforge.net/

object FileEvents {

    // val watch = watchModified("dir") { name =>
    //     println("modified: " + name)
    // }
    // 
    // watch.stop
    //
    def watchCreated(directory: String, recursive: Boolean = false)(f: (String) => Unit) =
        new FileWatcher(directory, recursive, what = JNotify.FILE_CREATED) {
            override def created(name: String) {f(name)}
        }

    def watchModified(directory: String, recursive: Boolean = false)(f: (String) => Unit) =
        new FileWatcher(directory, recursive, what = JNotify.FILE_MODIFIED) {
            override def modified(name: String) {f(name)}
        }

    def watchDeleted(directory: String, recursive: Boolean = false)(f: (String) => Unit) =
        new FileWatcher(directory, recursive, what = JNotify.FILE_DELETED) {
            override def deleted(name: String) {f(name)}
        }

    def watchRenamed(directory: String, recursive: Boolean = false)(f: (String, String) => Unit) =
        new FileWatcher(directory, recursive, what = JNotify.FILE_RENAMED) {
            override def renamed(oldName: String, newName: String) {f(oldName, newName)}
        }
        
    def watch(directory: String, recursive: Boolean = false)(f: (String) => Unit) = 
        new FileWatcher(directory, recursive, what = JNotify.FILE_ANY) {
            override def created(name: String) = f(name)
            override def modified(name: String) = f(name)
            override def deleted(name: String) = f(name)
        }

    def watchFile(path: String)(f: => Unit) = {
        val parts = path.splitAt(path.lastIndexOf("/"))
        val filename = parts._2.splitAt(1)._2 // XXX: This is VERY ugly
        print("Watching filename: %s of given path: %s".format(filename, path))

        new FileWatcher(parts._1, false, what = JNotify.FILE_ANY) {
            
            override def created(name: String) = if(name == filename) f
            override def modified(name: String) = if(name == filename) f
            override def deleted(name: String) = if(name == filename) f
        }
    }
}
