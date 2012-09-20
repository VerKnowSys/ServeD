package com.verknowsys.served.utils

/**
    @author tallica
 */
object SvdSymlink {


    def makeSymlink(source: String, destination: String) = {
        CLibrary.instance.symlink(source, destination) match {
            case 0 => true
            case _ => false
        }
    }


    def isSymlink(path: String) = CSymlink.instance.isSymlink(path)


    def getSymlinkDestination(path: String) = {
        val bufSize = 512
        var destination = new Array[Byte](bufSize)
        val pathSize = CLibrary.instance.readlink(path, destination, bufSize)
        new String(destination.take(pathSize).map(_.toChar))
    }

}
