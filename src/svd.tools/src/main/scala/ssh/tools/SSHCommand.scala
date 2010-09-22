// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.utils.ssh.tools


import com.verknowsys.served.utils.deployer.Deployer
import com.verknowsys.served.utils.skeletons.JNLPSkeleton
import com.verknowsys.served.utils.signals.{Init, Quit}
import com.verknowsys.served.utils.jar.comparator.JarEntryComparator
import com.verknowsys.served.utils.prefs.Preferences
import com.verknowsys.served.utils.Utils
import com.verknowsys.served.utils.version.CddsVersion

import actors.Actor
import java.net.UnknownHostException
import com.sshtools.j2ssh.authentication.{PasswordAuthenticationClient, AuthenticationProtocolState}
import com.sshtools.j2ssh.SshClient
import java.io.{BufferedReader, InputStreamReader, File}
import java.util.{Date, ArrayList}
import org.apache.log4j.Logger

/**
 * User: dmilith
 * Date: Jun 27, 2009
 * Time: 3:15:04 PM
 */

object SSHCommand extends Utils {

	val prefs = Deployer.prefs
	override def logger = Logger.getLogger(SSHCommand.getClass)
	initLogger
	val buildTextFile = "build.text" // XXX hardcoded but probably it will stay hardcoded anyway ;}
	val host = prefs.get("sshHost")
	val port = prefs.geti("sshPort")
	val userName = prefs.get("sshUserName")
	val password = prefs.get("sshPassword")
	val ssh = new SshClient


	def backup = {
		val clientForRemoteCommand = ssh.openSessionChannel
		val backupDate = (new Date).toString.replaceAll(" |:", "_")
		val source = prefs.get("remoteWebStartDeployDir")
		val destination = prefs.get("remoteWebStartDeployDir") + "../OLD/" + backupDate
		logger.info("Copying " + source + " to " + destination)
		clientForRemoteCommand.executeCommand("cp -r " + source + " " + destination)
		clientForRemoteCommand.close
	}


	def deploy(list: ArrayList[File], deployTmpDir: String) = {
		val remoteDeployDir = prefs.get("remoteWebStartDeployDir") + "lib/"
		val listOfSignedFiles = list.toArray.map{ a => deployTmpDir + a.toString.split("/").last }
		val clientForRemoteCommand = ssh.openSessionChannel
		clientForRemoteCommand.executeCommand("mkdir -p " + remoteDeployDir)  // make sure that directories exist
		clientForRemoteCommand.close
		backup

		def actionBlock(localFile: String): Unit = {
			val clientForRemoteCommand = ssh.openSessionChannel
			val comparator = new JarEntryComparator
			var listOfCRCLocalFile = comparator.loadAndThrowListOfCrcs(localFile) // XXX: variable
			clientForRemoteCommand.executeCommand(
				prefs.get("remoteProjectToolsDir") + "getcrcs" + " " +
				remoteDeployDir + localFile.split("/").last + " " +
				prefs.get("remoteScalaBin"))
			val input = new BufferedReader(new InputStreamReader(clientForRemoteCommand.getInputStream))
			var output = "" // XXX: variable
			var line = "" // XXX: variable
			while (line != null) {
				output += line
				line = input.readLine
			}
			input.close

			var out = List[String]() // XXX: variable
			output.split(",").foreach{ a => out ++= List[String](a) }
			logger.debug("1: " + out)
			logger.debug("2: " + listOfCRCLocalFile)
			logger.debug("result: " + (listOfCRCLocalFile -- out))
			if ((out -- listOfCRCLocalFile) == List()) {
				logger.info("FILE IDENTICAL: " + localFile.split("/").last)
			} else {
				logger.warn("FILE DIFFERENT: " + localFile.split("/").last)
				logger.info("Uploading " + localFile.split("/").last)
				putLocalFileToRemoteHost(localFile, remoteDeployDir + localFile.split("/").last)
			}
			clientForRemoteCommand.close
		}
		logger.info("Deploying")
		listOfSignedFiles foreach {
			actionBlock(_)
		}
		logger.info("Verifying deploy")
		listOfSignedFiles foreach {
			actionBlock(_)
		}
		// deploying jnlp file
		logger.info("Generating JNLP file")
		var arguments = "" // XXX: variable
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
		val tempJnlpFileName = "/tmp/launch-" + Deployer.uuid + ".jnlp"
		logger.debug("Temporary jnlp filename: " + tempJnlpFileName)
		jnlp.saveJNLP(tempJnlpFileName)
		logger.warn("Putting JNLP file to remote server")
		putLocalFileToRemoteHost(tempJnlpFileName, prefs.get("remoteWebStartDeployDir") + prefs.get("jnlpFileName") )
	}


	def putLocalFileToRemoteHost(source: String, destination: String) = {
		val client = ssh.openSftpClient
		logger.debug("Source: " + source + " to destination: " + destination + " on connection: " + client)
		client.put(source, destination )
		client.quit
		logger.debug("File sent to remote host")
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


	def connect =
		try {
			logger.info("Connecting to host " + host + ":" + port)
			ssh.connect(host, port)
		} catch {
			case x: UnknownHostException =>
				logger.error("Couldn't connect to remote host: " + host + " at port " + port)
				exit
		}

	
	def disconnect = ssh.disconnect
	
}