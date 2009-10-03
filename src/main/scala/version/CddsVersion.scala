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


abstract class CddsVersion(
		val configFile: String
		) extends Utils {
	
	lazy val prefs = new Preferences(configFile)
	lazy val debug = prefs.getb("debug")
	lazy val repositoryDir = prefs.get("gitRepositoryProjectDir")
	lazy val gitExecutable = prefs.get("gitExecutable")
	lazy val buildTextFile = "build.text"
//	lazy val resourceBuildFile = prefs.get("jnlpCodebase") + buildTextFile // keep build.text file on remote server only in home of application url
//	lazy val file = new File("/tmp/" + buildTextFile)
	lazy val shaCommand = Array(gitExecutable, "--git-dir=" + repositoryDir, "rev-list", "--no-merges", "HEAD...HEAD~1") // get newest commit sha
	lazy val outputSha = CommandExec.cmdExec(shaCommand).trim.split("\n")(0)
	lazy val outputHostname = CommandExec.cmdExec(Array("hostname", "-s"))

	def this() = this ("project.tools.xml")
	def versionPrefix: String

	def currentVersion = {
		lazy val buildFileResource = getClass.getResource("/codadris/" + buildTextFile)
		logger.info("Build file Resource: " + buildFileResource)
		if (buildFileResource != null) {
			lazy val fileFromResource = buildFileResource.getPath
			lazy val source = Source.fromFile(fileFromResource).mkString
			logger.info("Build file contents: " + source)
			versionPrefix + source  
		} else {
			versionPrefix + "dev"
		}
	}


	//	/**
	//	 * downloadVersion will get version from remote build.text file.
	//	 */
	//	def downloadVersion = {
	//		var lines = ""
	//		try {
	//			for (line <- Source.fromURL(resourceBuildFile).getLines) {
	//				lines += line
	//			}
	//			lines
	//		} catch {
	//			case _ =>
	//				"UnKnown"
	//		}
	//	}

	//	try {
	//		Source.fromURL(resourceBuildFile) // check for existance
	//	} catch {
	//		case x: Exception => {
	//			logger.warn("Remote " + buildTextFile + " not found. Creating new file ")
	//			writeNewContentToBuildFile(0)
	//		}
	//	}

	//	def createJarFileWithCurrentVersion = {
	//		lazy val versionFromURL = downloadVersion
	//		writeNewContentToBuildFile(downloadVersion.split("##")(1).toInt)
	//		lazy val createJarCommand = Array(
	//			prefs.get("jarExecutable"), "cf", "project-version.jar", "/tmp/" + buildTextFile
	//			)
	//		logger.warn(CommandExec.cmdExec(createJarCommand).trim)
	//		downloadVersion
	//	}

	//
	//	/**
	//	 * getVersionFromJar will get version from jar file, which is compiled in jar as resource file
	//	 */
	//	def readFromJARFile(filename: String) = {
	//		lazy val is = getClass.getResourceAsStream(filename)
	//        lazy val isr = new InputStreamReader(is)
	//        lazy val br = new BufferedReader(isr)
	//        lazy val sb = new StringBuffer
	//        var line = ""
	//		try {
	//            sb.append(br.readLine) // only one - first line is interesting
	//		} catch {
	//			case x: Exception =>
	//		}
	//        br.close
	//        isr.close
	//        is.close
	//        sb.toString
	//	}

	//	def getVersionFromJar = {
	//		try {
	//			// XXX: temporarely:
	//			downloadVersion
	////			readFromJARFile(buildTextFile)
	//		} catch {
	//			case x: Exception => {
	//				logger.warn("Exception occured while trying to read file from jar: " + x)
	//				// TODO: create new jar file with current version
	//				createJarFileWithCurrentVersion
	//			}
	//		}
	//	}
	//
	//	def getVersionBuild: Int = {
	//		getVersionFromJar.split("##")(1).toInt
	//	}
	//
	//	def getVersion(prefix: String): String = {
	//		val ver = getVersionFromJar
	//		prefix + ver.split("##")(1) + " (" + ver.split("##")(2) + ")"
	//	}
	//
	//
	//	/**
	//	 * getVersionFull will get full version from jar file, which is compiled in jar as resource file
	//	 */
	//	def getVersionFull: String = {
	//		lazy val ver = getVersionFromJar
	//		"Compiled at" + ": " + ver.split("##")(0) +
	//				", Build: " + ver.split("##")(1) + ", " +
	//				"Last" + " Sha1: " + ver.split("##")(2) +
	//				", on: " + ver.split("##")(3)
	//	}
	//

	//	def writeNewContentToBuildFile(p_buildNumber: Int) = {
	//		withPrintWriter(file) {
	//			writer => writer.print(
	//				(new Date).toString + "##" +
	//				(p_buildNumber).toString + "##" +
	//				outputSha + "##" +
	//				outputHostname.trim
	//			)
	//		}
	//	}


	//	def loadAndUpdateVersion = {
	//		try {
	//			logger.warn("Build file located in: " + resourceBuildFile)
	//			for (line <- Source.fromURL(resourceBuildFile).getLines) {
	//				logger.warn("Old version: " + getVersionFromJar)
	//				logger.warn("Current build number: " + (buildNumber + 1))
	//				writeNewContentToBuildFile(buildNumber + 1)
	//			}
	//		} catch {
	//			case x: Throwable => {
	//				logger.warn("Error occured in intialization: Cannot open " + buildTextFile + " file. Generating new one")
	//				writeNewContentToBuildFile(buildNumber + 1)
	//			}
	//		}
	//	}

}