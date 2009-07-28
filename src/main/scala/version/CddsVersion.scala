package version

import command.exec.CommandExec
import org.apache.log4j._
import _root_.java.io.{OutputStreamWriter, PrintWriter, File}
import _root_.java.util.Date
import io.Source
import prefs.Preferences
import signals.{Init, Quit}

import utils.Utils

/**
 * User: dmilith
 * Date: May 19, 2009
 * Time: 4:48:57 PM
 */


trait CddsVersion extends Utils {

	var logger = Logger.getLogger(classOf[CddsVersion])
	val prefs = (new Preferences).loadPreferences
	val debug = prefs.getb("debug")
	val repositoryDir = prefs.get("gitRepositoryProjectDir")
	val gitExecutable = prefs.get("gitExecutable")
	val buildTextFile = "build.text" // XXX: hardcoded
	val resourceBuildFile = prefs.get("jnlpCodebase") + buildTextFile // keep build.text file on remote server only in home of application url
	val buildNumber = getVersionBuild
	val file = new File("/tmp/" + buildTextFile)
	val shaCommand = Array(gitExecutable,  "--git-dir=" + repositoryDir, "rev-list", "--no-merges", "HEAD...HEAD~1") // get newest commit sha
	val outputSha = CommandExec.cmdExec(shaCommand).trim.split("\n")(0)
	val outputHostname = CommandExec.cmdExec(Array("hostname", "-s"))
	try {
		Source.fromURL(resourceBuildFile) // check for existance
	} catch {
		case x: Exception => {
			logger.warn("Remote " + buildTextFile + " not found. Creating new file ")
			writeNewContentToBuildFile(0)
		}
	}


	/**
	 * getVersion will get version from jar file, which is compiled in jar as resource file
	 */
	def getVersion: String = {
		getVersion("2.0.") // XXX: hardcoded	
	}


	def getVersionBuild: Int = {
		var line = ""
		try {
			for (lines <- Source.fromURL(resourceBuildFile).getLines) {
				line = lines
			}
			return line.split("##")(1).toInt
		} catch {
			case _ =>
				return 0;
		}
	}


	def getVersion(prefix: String): String = {
		var line = ""
		try {
			for (lines <- Source.fromURL(resourceBuildFile).getLines) {
				line = lines
			}
			return prefix + line.split("##")(1) + " (" + line.split("##")(2) + ")"
		} catch {
		    case _ =>
			    return "Unknown";
		}
	}


	/**
	 * getVersionFull will get full version from jar file, which is compiled in jar as resource file
	 */
	def getVersionFull: String = {
		var line = ""
		try {
			for (lines <- Source.fromURL(resourceBuildFile).getLines) {
				line = lines
			}
			return "Compiled at" + ": " + line.split("##")(0) + ", Build: " +
						line.split("##")(1) + ", " + "Last" + " Sha1: " + line.split("##")(2) + ", on: " + line.split("##")(3)
		} catch {
			case _ =>
				return "Unknown";
		}
	}

	
	def writeNewContentToBuildFile(p_buildNumber: Int) = {
		withPrintWriter(file) {
			writer => writer.print(
				(new Date).toString + "##" +
				(p_buildNumber).toString + "##" +
				outputSha + "##" +
				outputHostname.trim
			)
		}
	}


	def loadAndUpdateVersion = {
		try {
			logger.warn("Build file located in: " + resourceBuildFile)
			for (line <- Source.fromURL(resourceBuildFile).getLines) {
				logger.warn("Old version: " + getVersion)
				logger.warn("Current build number: " + (buildNumber + 1))
				writeNewContentToBuildFile(buildNumber + 1)
			}
		} catch {
			case x: Throwable => {
				logger.warn("Error occured in intialization: Cannot open " + buildTextFile + " file. Generating new one")
				writeNewContentToBuildFile(buildNumber + 1)
			}
		}
	}

}