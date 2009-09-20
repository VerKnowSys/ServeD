package version

import command.exec.CommandExec
import java.io._
import java.util.{UUID, Date}
import org.apache.log4j._
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

	val MAJOR_VERSION = "2.0"
	override
	def logger = Logger.getLogger(classOf[CddsVersion])
	val prefs = new Preferences
	val debug = prefs.getb("debug")
	val repositoryDir = prefs.get("gitRepositoryProjectDir")
	val gitExecutable = prefs.get("gitExecutable")
	val buildTextFile = "build.text"
	val resourceBuildFile = prefs.get("jnlpCodebase") + buildTextFile // keep build.text file on remote server only in home of application url
	val buildNumber = getVersionBuild
	val file = new File("/tmp/" + buildTextFile)
	val shaCommand = Array(gitExecutable,  "--git-dir=" + repositoryDir, "rev-list", "--no-merges", "HEAD...HEAD~1") // get newest commit sha
	val outputSha = CommandExec.cmdExec(shaCommand).trim.split("\n")(0)
	val outputHostname = CommandExec.cmdExec(Array("hostname", "-s"))
//	try {
//		Source.fromURL(resourceBuildFile) // check for existance
//	} catch {
//		case x: Exception => {
//			logger.warn("Remote " + buildTextFile + " not found. Creating new file ")
//			writeNewContentToBuildFile(0)
//		}
//	}


	/**
	 * downloadVersion will get version from repote build.text file.
	 */
	def downloadVersion: String = {
		var lines = ""
		try {
			for (line <- Source.fromURL(resourceBuildFile).getLines) {
				lines += line
			}
			lines
		} catch {
			case _ =>
				"UnKnown"
		}
	}


	def createJarFileWithCurrentVersion = {
		val versionFromURL = downloadVersion
		writeNewContentToBuildFile(downloadVersion.split("##")(1).toInt)
		val createJarCommand = Array(
			prefs.get("jarExecutable"), "cf", "project-version.jar", "/tmp/" + buildTextFile
			)
		logger.warn(CommandExec.cmdExec(createJarCommand).trim)
		downloadVersion
	}

	/**
	 * getVersionFromJar will get version from jar file, which is compiled in jar as resource file
	 */
	def readFromJARFile(filename: String) = {
		val is = getClass.getResourceAsStream(filename)
        val isr = new InputStreamReader(is)
        val br = new BufferedReader(isr)
        val sb = new StringBuffer
        var line = ""
		try {
            sb.append(br.readLine) // only one - first line is interesting
		} catch {
			case x: Exception =>
		}
        br.close
        isr.close
        is.close
        sb.toString
	}

	def getVersionFromJar = {
		try {
//			readFromJARFile(buildTextFile)
		} catch {
			case x: Exception => {
				logger.warn("Exception occured while trying to read file from jar: " + x)
				// TODO: create new jar file with current version
				createJarFileWithCurrentVersion
			}
		}
	}

	def getVersionBuild: Int = {
		getVersionFromJar.split("##")(1).toInt
	}

	def getVersion(prefix: String): String = {
		val ver = getVersionFromJar
		prefix + ver.split("##")(1) + " (" + ver.split("##")(2) + ")"
	}


	/**
	 * getVersionFull will get full version from jar file, which is compiled in jar as resource file
	 */
	def getVersionFull: String = {
		val ver = getVersionFromJar
		"Compiled at" + ": " + ver.split("##")(0) +
				", Build: " + ver.split("##")(1) + ", " +
				"Last" + " Sha1: " + ver.split("##")(2) +
				", on: " + ver.split("##")(3)
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
				logger.warn("Old version: " + getVersionFromJar)
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