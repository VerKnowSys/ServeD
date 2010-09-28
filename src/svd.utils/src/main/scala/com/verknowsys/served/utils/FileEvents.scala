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
        new FileWatcher(directory, recursive, what = JNotify.FILE_CREATED){
            override def created(name: String){ f(name) }
        }

    def watchModified(directory: String, recursive: Boolean = false)(f: (String) => Unit) =
        new FileWatcher(directory, recursive, what = JNotify.FILE_MODIFIED){
            override def modified(name: String){ f(name) }
        }
    
    def watchDeleted(directory: String, recursive: Boolean = false)(f: (String) => Unit) =
        new FileWatcher(directory, recursive, what = JNotify.FILE_DELETED){
            override def deleted(name: String){ f(name) }
        }
    
    def watchRenamed(directory: String, recursive: Boolean = false)(f: (String, String) => Unit) =
        new FileWatcher(directory, recursive, what = JNotify.FILE_RENAMED){
            override def renamed(oldName: String, newName: String){ f(oldName, newName) }
        }
       
}

// new FileWatcher("dir", what = JNotify.FILE_MODIFIED){
//     override def created(name: String){
//         
//     }
//     
//     override def modified(name: String){
//         
//     }
//     
//     override def deleted(name: String){
//         
//     }
//     
//     override def renamed(oldName: String, newName: String){
//         
//     }
// }
class FileWatcher(directory: String, recursive: Boolean = false, what: Int = JNotify.FILE_ANY){
    val watchID = JNotify.addWatch(directory, what, recursive, new JNotifyListener {
        def fileRenamed(wd: Int, rootPath: String, oldName: String, newName: String) = renamed(oldName, newName)
        def fileModified(wd: Int, rootPath: String, name: String) = modified(name)
        def fileDeleted(wd: Int, rootPath: String, name: String) = deleted(name)
        def fileCreated(wd: Int, rootPath: String, name: String) = created(name)
    })
    
    def created(name: String){}
    
    def modified(name: String){}
    
    def deleted(name: String){}
    
    def renamed(oldName: String, newName: String){}
    
    def stop = JNotify.removeWatch(watchID)
}




