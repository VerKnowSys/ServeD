// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package ssh.tools


import actors.Actor
import com.sshtools.j2ssh.authentication.{PasswordAuthenticationClient, AuthenticationProtocolState}
import com.sshtools.j2ssh.SshClient
import deployer.Deployer
import jar.comparator.JarEntryComparator
import java.io.{BufferedReader, InputStreamReader, File}

import java.util.ArrayList
import org.apache.log4j.Logger
import prefs.{Preferences, PreferencesActor}
/**
 * User: dmilith
 * Date: Jun 27, 2009
 * Time: 3:15:04 PM
 */

object SSHActor extends Actor {

	private val logger = Logger.getLogger(SSHActor.getClass)
	private var prefs: Preferences = null
	private var debug: Boolean = false
	private var host = ""
	private var port = 22
	private var userName = ""
	private var password = ""
	private var uuid = ""
	val ssh = new SshClient

	override
	def act = {
		Actor.loop {
			react {
				case s: Preferences => {
					prefs = s
					debug = prefs.getb("debug")
					host = prefs.get("sshHost")
					port = prefs.geti("sshPort")
					userName = prefs.get("sshUserName")
					password = prefs.get("sshPassword")
					connect
					auth
					act
				}
				case 'Quit => {
					PreferencesActor ! 'Quit
					disconnect
					Deployer ! 'Quit
					exit
				}
				case ('PerformTasks, x: ArrayList[File], deployUuid: String, deployDir: String) => {
					logger.info("Performing tasks with given list of files: " + x.toArray.map{ a => deployDir + a.toString.split("/").last })
					logger.info("Remote dir containing jars: " + prefs.get("remoteWebStartDeployDir") + "lib/")
					uuid = deployUuid
					performRemoteTasksAndQuit(x, deployUuid, deployDir)
					act
				}
			}
		}
	}

	def performRemoteTasksAndQuit(list: ArrayList[File], deployUuid: String, deployDir: String) = {

		val remoteDeployDir = prefs.get("remoteWebStartDeployDir") + "lib/"
		val listOfSignedFiles = list.toArray.map{ a => deployDir + a.toString.split("/").last }

		logger.warn(listOfSignedFiles.toArray)
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
			if ((List(output) -- listOfCRCLocalFile) == List()) {
				logger.warn("FILE IDENTICALLY: " + localFile.split("/").last)
			} else {
				logger.warn("FILE DIFFERENT: " + localFile.split("/").last)
			}
			clientForRemoteCommand.close
		}




		//Open the SFTP channel
		//		val client = ssh.openSftpClient
		//Send the file
		//		client.put("/tmp/FileCache/http:__www_google_pl_images_nav_logo4_png","/tmp/dupa" + uuid + ".png")
		//disconnect
		//		client.quit
		this ! 'Quit
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