package version

import command.exec.CommandExec
import java.net.URL
import org.apache.log4j._
import _root_.java.io.{OutputStreamWriter, PrintWriter, File}
import _root_.java.util.Date
import io.Source
import prefs.Preferences
import signals.{Init, Quit}

import ssh.tools.SSHActor
import utils.Utils

/**
 * User: dmilith
 * Date: May 19, 2009
 * Time: 4:48:57 PM
 */


object CddsVersion extends Application with Utils {
	var logger = Logger.getLogger(CddsVersion.getClass)
	initLogger
	val prefs = new Preferences
	val debug = prefs.getb("debug")
	if (debug) {
		setLoggerLevelDebug(Level.TRACE) // XXX: hardcoded
	}
	val repositoryDir = prefs.get("gitRepositoryProjectDir")
	val gitExecutable = prefs.get("gitExecutable")
	val buildFileLocationRemote = prefs.get("jnlpCodebase") // keep build.text file on remote server only in home of application url
	val buildTextFile = "build.text" // XXX: hardcoded
	val resourceBuildFile = buildFileLocationRemote + buildTextFile
	try {
		Source.fromURL(resourceBuildFile) // check for existance
	} catch {
		case x: Exception => {
			logger.error("Remote " + buildTextFile + " not found. Creating new file " + "\n" + x.printStackTrace)
			// TODO: file creation code here
				logger.info("NYI")
		}
	}
	val buildNumber = getVersionBuild
	logger.info("Build number: " + buildNumber)


	/**
	 * getVersion will get version from jar file, which is compiled in jar as resource file
	 */
	def getVersion: String = {
		getVersion("2.0.") // XXX: hardcoded	
	}

	def getVersionBuild: Int = {
		var line = ""
		if ( resourceBuildFile != null ) {
			for (lines <- Source.fromURL(resourceBuildFile).getLines) {
				line = lines
			}
			return line.split("##")(1).toInt
		} else {
			return 0;
		}
	}

	def getVersion(prefix: String): String = {
		var line = ""
		if ( resourceBuildFile != null ) {
			for (lines <- Source.fromURL(resourceBuildFile).getLines) {
				line = lines
			}
			return prefix + line.split("##")(1) + " (" + line.split("##")(2) + ")"
		} else {
			return "Unknown";
		}
	}

	/**
	 * getVersionFull will get full version from jar file, which is compiled in jar as resource file
	 */
	def getVersionFull: String = {
		var line = ""
		if ( resourceBuildFile!= null ) {
			for (lines <- Source.fromURL(resourceBuildFile).getLines) {
				line = lines
			}
			return "Compiled at" + ": " + line.split("##")(0) + ", Build: " +
						line.split("##")(1) + ", " + "Last" + " Sha1: " + line.split("##")(2) + ", on: " + line.split("##")(3)
		} else {
			return "Unknown";
		}
	}

	def withPrintWriter(file: File)(op: PrintWriter => Unit) = {
		val writer = new PrintWriter(file)
		try {
			op(writer)
		} finally {
			writer.close
		}
	}


	def writeNewContentToBuildFile = {
		val file = new File("/tmp/" + buildTextFile)
		val shaCommand = Array(gitExecutable,  "--git-dir=" + repositoryDir, "rev-list", "--no-merges", "HEAD...HEAD~1") // get newest commit sha
		val outputSha = CommandExec.cmdExec(shaCommand).trim.split("\n")(0)
		val hostnameCommand = Array("hostname", "-s")
		val outputHostname = CommandExec.cmdExec(hostnameCommand)
		withPrintWriter(file) {
			writer => writer.print(
				(new Date).toString + "##" +
				(buildNumber + 1).toString + "##" +
				outputSha + "##" +
				outputHostname.trim
			)
		}
	}


	def updateRemoteVersion = {
		SSHActor.start
		SSHActor ! Init
		SSHActor ! ("/tmp/" + buildTextFile, prefs.get("remoteWebStartDeployDir") + buildTextFile )
	}


	def loadAndUpdate = {
		try {
			logger.warn("Build file located in: " + resourceBuildFile)
			for (line <- Source.fromURL(resourceBuildFile).getLines) {
				logger.warn("Old version: " + getVersion)
				logger.warn("Current build number: " + (buildNumber + 1))
				writeNewContentToBuildFile
				updateRemoteVersion
				logger.warn("Updated successfully")
			}
		} catch {
			case x: Throwable => {
				logger.error("Error occured in intialization: Cannot open " + buildTextFile + " file. Generating new one", x)
				writeNewContentToBuildFile
				updateRemoteVersion
				logger.warn("Updated successfully")
			}
		}
	}

	loadAndUpdate

}