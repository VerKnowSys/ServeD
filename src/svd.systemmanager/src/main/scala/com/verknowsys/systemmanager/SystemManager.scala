// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.systemmanager

/**
 * User: dmilith
 * Date: Dec 12, 2009
 * Time: 1:34:27 AM
 */

import com.verknowsys.served.utils.Utils
import com.sun.jna.Library
import com.sun.jna.Native
import java.nio.charset.Charset


object SystemManager extends Utils {

	/**Simple example of native C POSIX library declaration and usage. */

	def posix = Native.loadLibrary("c", classOf[POSIX]).asInstanceOf[POSIX]
	
	trait POSIX extends Library {
		def chmod(filename: String, mode: Int)

		def chown(filename: String, user: Int, group: Int)

		def touch(filename: String)

		def rename(oldpath: String, newpath: String)

		def kill(pid: Int, signal: Int): Int

		//def link(oldpath: String, newpath: String): Int

		def mkdir(path: String, mode: Int): Int

		def execl(comm: String): Int
		
		def rmdir(path: String): Int

	}

	def main(args: Array[String]) {

		posix.mkdir("/tmp/newdir", 0777)
		posix.rename("/tmp/newdir", "/tmp/renamedir")
		posix.touch("/tmp/renamedir/file1")
		posix.chmod("/tmp/renamedir/file1", 0755)
		
	}

}