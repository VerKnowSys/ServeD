// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package ssh.tools


import actors.Actor
import cases.{Init, Quit}

import com.sshtools.j2ssh.authentication.{PasswordAuthenticationClient, AuthenticationProtocolState}
import com.sshtools.j2ssh.SshClient
import deployer.{JNLPSkeleton, Deployer}
import jar.comparator.JarEntryComparator
import java.io.{BufferedReader, InputStreamReader, File}

import java.util.ArrayList
import org.apache.log4j.Logger
import prefs.Preferences

/**
 * User: dmilith
 * Date: Jun 27, 2009
 * Time: 3:15:04 PM
 */

object SSHActor extends Actor {

	private val logger = Logger.getLogger(SSHActor.getClass)
	private val prefs: Preferences = (new Preferences).loadPreferences
	private val debug = prefs.getb("debug")
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
					act
				}
			}
		}
	}

	def deploy(list: ArrayList[File], deployUuid: String, deployTmpDir: String) = {

		val remoteDeployDir = prefs.get("remoteWebStartDeployDir") + "lib/"
		val listOfSignedFiles = list.toArray.map{ a => deployTmpDir + a.toString.split("/").last }

		listOfSignedFiles foreach { localFile =>
			val clientForRemoteCommand = ssh.openSessionChannel
			val comparator = new JarEntryComparator
			var listOfCRCLocalFile = comparator.loadAndThrowListOfCrcs(localFile)	

			clientForRemoteCommand.executeCommand(
				prefs.get("remoteProjectToolsDir") + "getcrcs" + " " +
				remoteDeployDir + localFile.split("/").last + " " +
				prefs.get("remoteProjectToolsDir") + " " +
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
				val client = ssh.openSftpClient
				client.put(localFile, remoteDeployDir + localFile.split("/").last)
				client.quit
			}
			clientForRemoteCommand.close
		}

		// deploying jnlp file
		logger.warn("Generating JNLP file")
		val client = ssh.openSftpClient
		var arguments = ""
		for( i <- prefs.getl("webstartArgumentsJVM")) { // TODO: XXX: maybe switch to normal String instead of List[String] 
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
		client.put(tempJnlpFileName, prefs.get("remoteWebStartDeployDir") + prefs.get("jnlpFileName") )
		client.quit
		this ! Quit
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