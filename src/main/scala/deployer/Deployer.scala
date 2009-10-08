// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package deployer


import actors._
import signals.{Init, Quit}
import command.exec.CommandExec
import jar.comparator.JarEntryComparator
import java.io._
import java.util.{UUID, Date, ArrayList}
import org.apache.commons.io.{FileUtils, CopyUtils}
import org.apache.log4j.{ConsoleAppender, Level, PatternLayout, Logger}
import java.util.regex.{Matcher, Pattern}
import prefs.Preferences
import skeletons.JNLPSkeleton
import ssh.tools.SSHCommand
import utils.Utils

/**
 * User: dmilith
 * Date: Jun 26, 2009
 * Time: 12:04:35 PM
 */


object Deployer extends Utils {

	val uuid = UUID.randomUUID.toString
	var prefs: Preferences = null
	Logger.getLogger("com.sshtools").setLevel(Level.WARN) // should quiet too verbose messages of sshtools

	override def logger = Logger.getLogger(Deployer.getClass)
	initLogger

	
	def main(args: Array[String]) {

		lazy val filesToBeDeployed = new ArrayList[File]()
		lazy val deployTmpDir = "/tmp/deployer-" + uuid + "/"
		lazy val pathToMaven2Repo = System.getProperty("user.home") + "/.m2/repository/"

		addShutdownHook {
			logger.debug("Into shutdown hook")
			logger.info("Finished.")
		}

		if (args.size == 0) {
			logger.error("Missing argument: (config filename)")
			exit
		}

		prefs = new Preferences(args(0))
		val debug = prefs.getb("debug")
		if (debug) {
			setLoggerLevelDebug(Level.TRACE)
		}

		lazy val basic_jar_names = prefs.getl("deployFilesBasic")
		lazy val dependency_jar_names = prefs.getl("deployFilesAdditionalDependencies")
		lazy val codebaseLocalDir = System.getProperty("user.home") + "/" + prefs.get("directoryForLocalDeploy")
		var macAppDeploy = false
		var basicOnly_? = prefs.getb("deployOnlyBasicFiles")

		logger.info("Starting Deployer.. v1.1")
		logger.info("Deployer mode: " + (if (debug) "DEBUG" else "NORMAL"))
		logger.info("Deployer home dir: " + System.getProperty("user.home") + "/.codadris/")
		logger.info("Maven 2 Repository dir: " + pathToMaven2Repo)
		logger.info("Deploy tmp dir: " + deployTmpDir)
		logger.info("Will deploy files to remote host to: " + prefs.get("remoteWebStartDeployDir"))
		logger.info("Given arguments:")
		args.foreach {
			logger.warn(_)
		}

		def remoteSSHDeploy = {
			SSHCommand.auth
			logger.debug("Performing tasks with given list of files: " + filesToBeDeployed.toArray.map {a => deployTmpDir + a.toString.split("/").last})
			logger.debug("Remote dir containing jars: " + prefs.get("remoteWebStartDeployDir") + "lib/")
			SSHCommand.deploy(filesToBeDeployed, deployTmpDir)
		}

		// check given arguments
		if (args.size == 1) {
			logger.warn("Requested to perform standard remote deploy")
			// normal deploy based on config values
			SSHCommand.connect
			getFilesFromMavenRepositoryAndSignThem
			remoteSSHDeploy
		} else {
			if (args.size == 2) {
				args(1) match {
					case "mac-app" => {
						logger.warn("Requested to perform Mac Application deploy")
						basicOnly_? = false
						macAppDeploy = true
						getFilesFromMavenRepositoryAndSignThem
						deployLocal
						copyAndInstallMacApp
						// TODO: implement Mac Application deploy support
					}
					case "windows-app" => {
						logger.warn("Requested to perform Windows Application deploy")
						basicOnly_? = false
						logger.error("Not yet implemented!")
						// TODO: implement deployment of windows application
					}
					case "linux-app" => {
						logger.warn("Requested to perform Linux Application deploy")
						basicOnly_? = false
						logger.error("Not yet implemented!")
						// TODO: implement deployment of linux application
					}
					case "local-full" => {
						// local full deploy without ssh actor
						logger.warn("Requested to perform FULL local deploy")
						basicOnly_? = false
						getFilesFromMavenRepositoryAndSignThem
						deployLocal
					}
					case "full" => {
						// remote full deploy without ssh actor
						logger.warn("Requested to perform FULL remote deploy")
						basicOnly_? = false
						SSHCommand.connect
						getFilesFromMavenRepositoryAndSignThem
						remoteSSHDeploy
					}
					case "local" => {
						// local deploy without ssh actor
						logger.warn("Requested to perform local deploy")
						getFilesFromMavenRepositoryAndSignThem
						deployLocal
					}
					case _ =>
						deployerHelp
				}
			}
		}

		def getFilesFromMavenRepositoryAndSignThem = {
			logger.info("Searching for jars in Maven repository and signing them..")
			(basic_jar_names ++ (if (!basicOnly_?) dependency_jar_names else Nil)).foreach {
				file =>
						logger.debug("*" + file + "*")
						findFile(new File(pathToMaven2Repo), new P {
							override
							def accept(t: String): Boolean = {
								val fileRegex = ".*" + file + "$"
								val pattern = Pattern.compile(fileRegex)
								val mat = pattern.matcher(t)
								if (mat.find) {
									if (!macAppDeploy)
										signJar(t)
									else { // don't sign jars when it's mac-app deploy
										logger.info("Copying jar: " + t.split("/").last)
										FileUtils.copyFileToDirectory(new File(t), new File(deployTmpDir)) // copy files to temporary dir
									}
									return true
								}
								return false
							}
						}, filesToBeDeployed)
			}
		}


		def signJar(fileToBeSigned: String) = {
			new File(deployTmpDir).mkdir // make temporary place for jars before signinig
			logger.info("Signing and copying jar: " + fileToBeSigned.split("/").last)
			FileUtils.copyFileToDirectory(new File(fileToBeSigned), new File(deployTmpDir)) // copy files to temporary dir
			val signCommand = Array(
				prefs.get("jarSignerExecutable"), "-storepass", prefs.get("jarSignerPassword"),
				deployTmpDir + fileToBeSigned.split("/").last, prefs.get("jarSignerKeyName")
				)
			logger.debug(CommandExec.cmdExec(signCommand).trim)
		}


		def backupLocal = {
			val backupDate = (new Date).toString.replaceAll(" |:", "_")
			val backupDir = new File(codebaseLocalDir + "../OLD_DEPLOYS/OLD_LOCAL_DEPLOY_" + backupDate + "/")
			val localDeployDir = new File(codebaseLocalDir)
			backupDir.mkdir
			localDeployDir.mkdir
			logger.info("Copying files from " + localDeployDir + " to " + backupDir)
			FileUtils.copyDirectory(localDeployDir, backupDir)
		}


		def deployLocal = {
			backupLocal
			filesToBeDeployed.toArray foreach {
				localFile => {
					val comparator = new JarEntryComparator
					comparator.load(deployTmpDir + localFile.toString.split("/").last, codebaseLocalDir + "lib/" + localFile.toString.split("/").last)
					if (comparator.diff_?) {
						logger.warn("File DIFFERENT: " + localFile.toString.split("/").last)
						FileUtils.copyFileToDirectory(new File(deployTmpDir + localFile.toString.split("/").last), new File(codebaseLocalDir + "lib/"))
					} else {
						logger.info("File IDENTICAL: " + localFile.toString.split("/").last)
					}
				}
			}
			if (!macAppDeploy) {
				logger.warn("Generating JNLP file")
				val arguments = prefs.getl("webstartArgumentsJVM").map{ a => a + " " }.mkString
				val jnlp = new JNLPSkeleton(
					prefs.get("jnlpMainClass"),
					prefs.get("jnlpAppName"),
					"file://" + codebaseLocalDir,
					prefs.get("jnlpFileName"),
					(prefs.getl("deployFilesBasic") ++ prefs.getl("deployFilesAdditionalDependencies")),
					arguments,
					prefs.get("jnlpVendor"),
					"file://" + codebaseLocalDir,
					prefs.get("jnlpIcon"),
					prefs.get("jnlpDescription")
					)
				val tempJnlpFileName = "/tmp/" + prefs.get("jnlpFileName")
				jnlp.saveJNLP(tempJnlpFileName)
				logger.info("Putting JNLP file to local deploy dir")
				FileUtils.copyFileToDirectory(new File(tempJnlpFileName), new File(codebaseLocalDir))
			}
		}


		def copyAndInstallMacApp = {
			logger.info("Copying jars to temporary mac app")
			FileUtils.copyDirectory(new File(deployTmpDir), new File(codebaseLocalDir + "../Coviob.app/lib")) // XXX: hardcoded Coviob.app name
			// XXX: application skeleton must be uploaded to ~/.codadris before deploy
			logger.info("Copying Application to /Applications")
			FileUtils.copyDirectory(new File(codebaseLocalDir + "../Coviob.app"), new File("/Applications/Coviob.app"))
			logger.info("Making executable of app starting script..")
			CommandExec.cmdExec(Array("chmod", "777", "/Applications/Coviob.app/Contents/MacOS/coviob2")) // XXX: why the fuck copying files will result changing permissions to files?!
		}


		def deployerHelp = logger.warn("\n\nDeployer quick help:\nValid params:\n" +
				"\tlocal -> for basic local deploy\n" +
				"\tlocal-full -> for full local deploy\n" +
				"\twindows-app -> for Windows application deploy\n" +
				"\tlinux-app -> for Linux (distro independent) application deploy\n" +
				"\tmac-app -> for Mac OS X application deploy and install\n" +
				"\tfull -> for full remote deploy\n" +
				"\tTo run remote deploy, no arguments (defaults based on xml config).")

		 exit
	}

}