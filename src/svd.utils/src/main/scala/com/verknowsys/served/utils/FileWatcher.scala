package com.verknowsys.served.utils

import net.contentobjects.jnotify._


class FileWatcher(directory: String, recursive: Boolean = false, what: Int = JNotify.FILE_ANY) {
    
    val watchID = JNotify.addWatch(directory, what, recursive, new JNotifyListener {
        
        // logger.debug("Watching directory: %s".format(directory))
        
        def fileRenamed(wd: Int, rootPath: String, oldName: String, newName: String) = renamed(oldName, newName)

        def fileModified(wd: Int, rootPath: String, name: String) = modified(name)

        def fileDeleted(wd: Int, rootPath: String, name: String) = deleted(name)

        def fileCreated(wd: Int, rootPath: String, name: String) = created(name)
    })

    def created(name: String) {}

    def modified(name: String) {}

    def deleted(name: String) {}

    def renamed(oldName: String, newName: String) {}

    def stop = JNotify.removeWatch(watchID)
}