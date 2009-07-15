// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package ssh.tools


import actors.Actor
import deployer.{Deployer, JNLPSkeleton}
import signals.{Init, Quit}

import com.sshtools.j2ssh.authentication.{PasswordAuthenticationClient, AuthenticationProtocolState}
import com.sshtools.j2ssh.SshClient
import jar.comparator.JarEntryComparator
import java.io.{BufferedReader, InputStreamReader, File}

import java.util.{Date, ArrayList}
import org.apache.log4j.Logger
import prefs.Preferences
import version.CddsVersion

/**
 * User: dmilith
 * Date: Jun 27, 2009
 * Time: 3:15:04 PM
 */

object SSHActor extends Actor with CddsVersion {

	private val host = prefs.get("sshHost")
	private val port = prefs.geti("sshPort")
	private val userName = prefs.get("sshUserName")
	private val password = prefs.get("sshPassword")
	private val ssh = new SshClient
	private var uuid = ""

	override
	def act = {
		Actor.loop {
			react {
				case Init => {
					connect
					auth
					act
				}
				case Quit => {
					disconnect
					Deployer ! Quit
					exit
				}
				case (x: ArrayList[File], deployUuid: String, deployTmpDir: String) => {
					logger.info("Performing tasks with given list of files: " + x.toArray.map{ a => deployTmpDir + a.toString.split("/").last })
					logger.info("Remote dir containing jars: " + prefs.get("remoteWebStartDeployDir") + "lib/")
					uuid = deployUuid
					deploy(x, deployUuid, deployTmpDir)
					loadAndUpdateVersion // update version in local temporary file
					logger.info("Putting build file to remote host..")
					putLocalFileToRemoteHost("/tmp/" + buildTextFile, prefs.get("remoteWebStartDeployDir") + buildTextFile)
					logger.warn("Remote file updated")
					act
				}
			}
		}
	}


	def backup = {
		val clientForRemoteCommand = ssh.openSessionChannel
		val backupDate = (new Date).toString.replaceAll(" |:", "_")
		val source = prefs.get("remoteWebStartDeployDir")
		val destination = prefs.get("remoteWebStartDeployDir") + "../OLD/" + backupDate
		logger.warn("Copying " + source + " to " + destination)
		clientForRemoteCommand.executeCommand("cp -r " + source + " " + destination)
		clientForRemoteCommand.close
	}


	def deploy(list: ArrayList[File], deployUuid: String, deployTmpDir: String) = {

		val remoteDeployDir = prefs.get("remoteWebStartDeployDir") + "lib/"
		val listOfSignedFiles = list.toArray.map{ a => deployTmpDir + a.toString.split("/").last }
		val clientForRemoteCommand = ssh.openSessionChannel
		clientForRemoteCommand.executeCommand("mkdir -p " + remoteDeployDir)  // make sure that directories exists
		clientForRemoteCommand.close
		backup

		def actionBlock(localFile: String): Unit = {
			val clientForRemoteCommand = ssh.openSessionChannel
			val comparator = new JarEntryComparator
			var listOfCRCLocalFile = comparator.loadAndThrowListOfCrcs(localFile)
			clientForRemoteCommand.executeCommand(
				prefs.get("remoteProjectToolsDir") + "getcrcs" + " " +
				remoteDeployDir + localFile.split("/").last + " " +
				prefs.get("remoteScalaBin"))
			val input = new BufferedReader(new InputStreamReader(clientForRemoteCommand.getInputStream))
			var output = ""
			var line = ""
			while (line != null) {
				output += line
				line = input.readLine
			}
			input.close

			var out = List[String]()
			output.split(",").foreach{ a => out ++= List[String](a) }
			logger.info("1: " + out)
			logger.info("2: " + listOfCRCLocalFile)
			logger.info("result: " + (listOfCRCLocalFile -- out))
			if ((out -- listOfCRCLocalFile) == List()) {
				logger.warn("FILE IDENTICAL: " + localFile.split("/").last)
			} else {
				logger.warn("FILE DIFFERENT: " + localFile.split("/").last)
				logger.warn("Uploading " + localFile.split("/").last)
				putLocalFileToRemoteHost(localFile, remoteDeployDir + localFile.split("/").last)
			}
			clientForRemoteCommand.close
		}
		logger.warn("Deploying")
		listOfSignedFiles foreach {
			actionBlock(_)
		}
		logger.warn("Verifying deploy")
		listOfSignedFiles foreach {
			actionBlock(_)
		}
		// deploying jnlp file
		logger.warn("Generating JNLP file")
		var arguments = ""
		for( i <- prefs.getl("webstartArgumentsJVM")) { // XXX: maybe switch to normal String instead of List[String] 
			arguments += i + " "
		}
		val jnlp = new JNLPSkeleton(
			prefs.get("jnlpMainClass"),
			prefs.get("jnlpAppName"),
			prefs.get("jnlpCodebase"),
			prefs.get("jnlpFileName"),
			(prefs.getl("deployFilesBasic") ++ prefs.getl("deployFilesAdditionalDependencies")),
			arguments,
			prefs.get("jnlpVendor"),
			prefs.get("jnlpHomePage"),
			prefs.get("jnlpIcon"),
			prefs.get("jnlpDescription")
			)
		val tempJnlpFileName = "/tmp/launch-" + uuid + ".jnlp"
		jnlp.saveJNLP(tempJnlpFileName)
		logger.warn("Putting JNLP file to remote server")
		putLocalFileToRemoteHost(tempJnlpFileName, prefs.get("remoteWebStartDeployDir") + prefs.get("jnlpFileName") )
		this ! Quit
	}


	def putLocalFileToRemoteHost(source: String, destination: String) = {
		val client = ssh.openSftpClient
		client.put(source, destination )
		client.quit
	}


	def auth = {
		val passwordAuthenticationClient = new PasswordAuthenticationClient
		passwordAuthenticationClient.setUsername(userName)
		passwordAuthenticationClient.setPassword(password)
		val result = ssh.authenticate(passwordAuthenticationClient)
		if (result != AuthenticationProtocolState.COMPLETE) {
			 logger.error("Login to " + host + ":" + port + " " + userName + "/" + password + " failed");
		}
	}


	def connect = ssh.connect(host, port)

	
	def disconnect = ssh.disconnect
}