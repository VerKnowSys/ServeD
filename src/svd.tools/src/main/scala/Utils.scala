// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.utils


import java.io.{PrintWriter, File, OutputStreamWriter}
import java.util.ArrayList
import java.util.regex.Pattern
import org.apache.log4j._
import com.verknowsys.served.utils.prefs.Preferences
import clime.messadmin.providers.sizeof.ObjectProfiler

/**
 * User: dmilith
 * Date: Jul 10, 2009
 * Time: 4:28:05 PM
 */



trait Utils {

	trait P { def accept(t: String): Boolean }

	lazy val logger = {
		initLogger
		Logger.getLogger(this.getClass)
	}
		
	val appender = new ConsoleAppender
	val level = Level.INFO

	def initLogger {
		appender.setName(ConsoleAppender.SYSTEM_OUT);
		appender.setWriter(new OutputStreamWriter(System.out))
		appender.setThreshold(level)
		appender.setLayout(new ANSIColorLayout("{ %-5p: [%c]: %m }%n"))
		if (Logger.getRootLogger.getAppender(ConsoleAppender.SYSTEM_OUT) == null) {
			Logger.getRootLogger.addAppender(appender)
		}
	}

	def setLoggerLevelDebug(arg: Level) = {
		appender.setThreshold(arg)
	}

	def withPrintWriter(file: File)(op: PrintWriter => Unit) = {
		val writer = new PrintWriter(file)
		try {
			op(writer)
		} finally {
			writer.close
		}
	}
	
	def addShutdownHook(block: => Unit) =
		Runtime.getRuntime.addShutdownHook( new Thread {
			override def run = block
		})

	def findFile(f: File, p: P, r: ArrayList[File]) {
		if (f.isDirectory && !(f.toString.contains("X11"))) { // XXX: hack to prevent entering endless recursion in directories like those stupid fucks from linux does with /usr/bin/X11
			val files = f.listFiles
			for (i <- 0 until files.length) {
				findFile(files(i), p, r)
			}
		} else if (p.accept(f + "")) {
			r.add(f)
		}
	}

	def pathsToSearchForExecutables = Array(
		new File("/opt/local/bin/"), // XXX: hardcoded paths
		new File("/bin/"),
		new File("/usr/bin/"),
		new File("/usr/local/bin/"),
		new File("/sw/bin/"),
		new File(System.getProperty("user.home") + "/bin"),
		new File(System.getProperty("user.home") + "/Bin")
	)

	def requirements = Array(
		("git", "gitExecutable"), // XXX: hardcoded executables
		("jarsigner", "jarSignerExecutable"),
		("jar", "jarExecutable")
	)

	def autoDetectRequirements(prefs: Preferences) = {
		for (i <- 0 until requirements.size)
			if (!(new File(prefs.get(requirements(i)._2)).exists)) {
				val al = new ArrayList[File]()
				if (System.getProperty("os.name").contains("Linux") ||
					System.getProperty("os.name").contains("Mac")) {
					for (path <- pathsToSearchForExecutables) {
						if (path.exists) {
							findFile( path, new P {
								override
								def accept(t: String): Boolean = {
									val fileRegex = ".*" + requirements(i)._1 + "$"
									val pattern = Pattern.compile(fileRegex)
									val mat = pattern.matcher(t)
									if ( mat.find ) return true
									return false
								}
							}, al)
						}
					}
					try {
						prefs.value.update(requirements(i)._2, al.toArray.first.toString)
					} catch {
						case x: NoSuchElementException => {
							logger.error(requirements(i)._1 + " executable not found")
						}
					}
				} else {
					logger.error("Windows hosts not yet supported")
					exit(1)
				}
			}
	}

	def sizeof(any: Any) = {
		ObjectProfiler.sizeof(any)
	}
	
}