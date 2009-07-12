package utils


import java.io.{File, OutputStreamWriter}
import java.util.ArrayList
import java.util.regex.Pattern
import org.apache.log4j._
/**
 * User: dmilith
 * Date: Jul 10, 2009
 * Time: 4:28:05 PM
 */



trait Utils {

	trait P { def accept(t: String): Boolean }

	val appender = new ConsoleAppender
	val level = Level.WARN

	def initLogger = {
		appender.setName(ConsoleAppender.SYSTEM_OUT);
		appender.setWriter(new OutputStreamWriter(System.out))
		appender.setThreshold(level)
		appender.setLayout(new PatternLayout("{ %-5p %d : %m }%n"));
		Logger.getRootLogger.addAppender(appender)
	}

	def setLoggerLevelDebug_?(arg: Priority) = {
		appender.setThreshold(arg)
	}

	def addShutdownHook(block: => Unit) =
		Runtime.getRuntime.addShutdownHook( new Thread {
			override def run = block
		})

	def findFile(f: File, p: P, r: ArrayList[File]) {
		if (f.isDirectory) {
			val files = f.listFiles
			for (i <- 0 until files.length) {
				findFile(files(i), p, r)
			}
		} else if (p.accept(f + "")) {
			r.add(f)
		}
	}

	def pathsToSearchForExecutables = Array(
		new File("/opt/local/bin/"), // XXX hardcoded paths
		new File("/bin/"),
		new File("/usr/bin/"),
		new File("/usr/local/bin/")
	)

	val requirements = Array(
		("git", "gitExecutable"),
		("jarsigner", "jarSignerExecutable")
	)

	def autoDetectRequirements = {
		for (i <- 0 until requirements.size)
			if (!(new File(prefs.get(requirements(i)._2)).exists)) {
				val al = new ArrayList[File]()
				if (System.getProperty("os.name").contains("Linux") ||
					System.getProperty("os.name").contains("Mac")) {
					for (path <- searchIn) {
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
	
}