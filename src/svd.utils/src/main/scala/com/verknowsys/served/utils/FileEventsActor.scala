package com.verknowsys.served.utils

import net.contentobjects.jnotify._

// See http://jnotify.sourceforge.net/
// run with $ mvn scala:run -DmainClass=com.verknowsys.served.utils.FileEvents 

object FileEvents {
    def main(args: Array[String]): Unit = {
        println("java.library.path = " + System.getProperty("java.library.path"))
        val mask = JNotify.FILE_CREATED  | 
                   JNotify.FILE_DELETED  | 
                   JNotify.FILE_MODIFIED | 
                   JNotify.FILE_RENAMED
        // watch subtree?
        val watchSubtree = true

        // add actual watch
        val watchID = JNotify.addWatch("/Users/teamon/Desktop", mask, watchSubtree, new FListener())
        
        Thread.sleep(1000000)
    }
}


class FListener extends JNotifyListener {
    def fileRenamed(wd: Int, rootPath: String, oldName: String, newName: String) {
      print("renamed " + rootPath + " : " + oldName + " -> " + newName)
    }
    
    def fileModified(wd: Int, rootPath: String, name: String) {
      print("modified " + rootPath + " : " + name)
    }
    
    def fileDeleted(wd: Int, rootPath: String, name: String) {
      print("deleted " + rootPath + " : " + name)
    }
    
    def fileCreated(wd: Int, rootPath: String, name: String) {
      print("created " + rootPath + " : " + name)
    }
    
    def print(msg: String) = System.err.println(msg)
}
