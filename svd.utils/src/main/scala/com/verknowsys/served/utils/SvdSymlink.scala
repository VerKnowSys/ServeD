package com.verknowsys.served.utils


import java.io.File

import com.verknowsys.served._
import com.verknowsys.served.utils._


/**
    @author tallica
 */
object SvdSymlink {


    implicit def convertAllFilesToString(some: File) = some.toString


    def makeSymlink(source: File, destination: File) = {
        CLibrary.instance.symlink(destination, source) match {
            case 0 => true
            case _ => false
        }
    }


    def isSymlink(path: File) = {
        import CUsageSys._
        CUsageSys.instance.isSymlink(path)
    }


    def getSymlinkDestination(path: File) = {
        val bufSize = 512
        var destination = new Array[Byte](bufSize)
        val pathSize = CLibrary.instance.readlink(path, destination, bufSize)
        new String(destination.take(pathSize).map(_.toChar))
    }

}
