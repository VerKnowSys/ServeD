package utils


import java.io.{File, OutputStreamWriter}
import java.util.ArrayList
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

}