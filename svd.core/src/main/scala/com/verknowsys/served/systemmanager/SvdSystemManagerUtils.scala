package com.verknowsys.served.systemmanager

import com.verknowsys.served._
import com.verknowsys.served.utils._
import java.io.File


object SvdSystemManagerUtils extends Logging {

    /** 
     * Changes owner of file at given path
     * 
     * @author dmilith
     */
    def chown(path: String, user: Int, group: Int = SvdConfig.defaultUserGroup, recursive: Boolean = true) =
        if (!(new File(path)).exists) {
            log.warn("Chown: File/ path doesn't exists! Cannot chown non existant file/ directory! IGNORING!")
            false
        } else {
            import CLibrary._
            val clib = CLibrary.instance
            val files = if (recursive) SvdUtils.recursiveListFilesFromPath(new File(path)).toList else (new File(path) :: Nil)
            log.trace("chown(path: %s, user: %d, group: %d, recursion: %s): File list: %s. Amount of files: %s".format(path, user, group, recursive, files.mkString(", "), files.length))

            for (file <- files) {
                log.trace("chowning: %s".format(file.getAbsolutePath))
                if (clib.chown(file.getAbsolutePath, user, group) != 0)
                    throw new Exception("Error occured while chowning: %s".format(file))
            }
            true
        }


    /** 
     * Changes permissions of file at given path
     * 
     * @author dmilith
     */
    def chmod(path: String, mode: Int, recursive: Boolean = true) =
        if (!(new File(path)).exists) {
            log.warn("Chmod: File or directory doesn't exists! Cannot chmod non existant file: '%s'! IGNORING!".format(path))
            false
        } else {
            import CLibrary._
            val clib = CLibrary.instance
            val files = if (recursive) SvdUtils.recursiveListFilesFromPath(new File(path)).toList else (new File(path) :: Nil)
            log.trace("chmod(path: %s, mode: %d, recursion: %s)".format(path, mode, recursive))

            for (file <- files) {
                log.trace("chmoding: %s".format(file.getAbsolutePath))
                if (clib.chmod(file.getAbsolutePath, mode) != 0)
                    throw new Exception("Error occured while chmoding: %s".format(file))
            }
            true
        }

}