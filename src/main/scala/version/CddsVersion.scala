package version

import command.exec.CommandExec
import java.io._
import java.util.{UUID, Date}
import org.apache.log4j._
import prefs.Preferences
import jar.JarAccess
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
	//	var sourceCache: String = null // XXX: not functional way
	lazy val getBuildDate = getLocalBuildFileContentsFromResource.split("##")(0)
	lazy val getBuildNumber = getLocalBuildFileContentsFromResource.split("##")(1).toInt
	lazy val getBuildSha = getLocalBuildFileContentsFromResource.split("##")(2)
	lazy val getBuildHostname = getLocalBuildFileContentsFromResource.split("##")(3)
	lazy val currentVersion = versionPrefix + getBuildNumber
	lazy val currentVersionFull =
	"Compiled at" + ": " + getBuildDate +
			", Build: " + getBuildNumber +
			", Git sha: " + getBuildSha +
			", on: " + getBuildHostname


	def versionPrefix: String

	def this() = this ("project.tools.xml")

	def getLocalBuildFileContentsFromResource = {
		try {
			lazy val fileFromResource = JarAccess.readLineFromJARFile("/codadris/" + buildTextFile)
			fileFromResource
		} catch {
			case x: FileNotFoundException =>
				logger.warn("File: build.text doesn't exists! " + x)
				"dev##1##dev##dev" // XXX: workaround to prevent ArrayOutOfBoundsException	
			case x: Throwable =>
				throw new UnsupportedOperationException("Unsupported error in CddsVersion: " + x)
				"dev##1##dev##dev" // XXX: workaround to prevent ArrayOutOfBoundsException
		}
	}



	//	def createJarFileWithCurrentVersion = {
	//		lazy val versionFromURL = downloadVersion
	//		writeNewContentToBuildFile(downloadVersion.split("##")(1).toInt)
	//		lazy val createJarCommand = Array(
	//			prefs.get("jarExecutable"), "cf", "project-version.jar", "/tmp/" + buildTextFile
	//			)
	//		logger.warn(CommandExec.cmdExec(createJarCommand).trim)
	//		downloadVersion
	//	}

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