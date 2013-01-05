/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.utils


import java.io.File


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
        CUsageSys.instance.isSymlink(path)
    }


    def getSymlinkDestination(path: File) = {
        val bufSize = 512
        var destination = new Array[Byte](bufSize)
        val pathSize = CLibrary.instance.readlink(path, destination, bufSize)
        new String(destination.take(pathSize).map(_.toChar))
    }

}
