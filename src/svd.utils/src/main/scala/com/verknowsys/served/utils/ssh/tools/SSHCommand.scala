// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.utils.ssh.utils


// import com.verknowsys.served.utils.deployer.Deployer
import com.verknowsys.served.utils.skeletons.JNLPSkeleton
import com.verknowsys.served.utils.signals.{Init, Quit}
import com.verknowsys.served.utils.jar.comparator.JarEntryComparator
import com.verknowsys.served.utils.Utils
// import com.verknowsys.served.utils.version.CddsVersion

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

    // val buildTextFile = "build.text" // XXX hardcoded but probably it will stay hardcoded anyway ;}
    val host = props("sshHost") getOrElse "127.0.0.1"
    val port = props("sshPort") getOrElse "22"
    val userName = props("sshUserName") getOrElse "guest"
    val password = props("sshPassword") getOrElse "nopassword"
    val ssh = new SshClient


    // def backup = {
    //  val clientForRemoteCommand = ssh.openSessionChannel
    //  val backupDate = (new Date).toString.replaceAll(" |:", "_")
    //  val source = props("remoteWebStartDeployDir")
    //  val destination = props("remoteWebStartDeployDir") + "../OLD/" + backupDate
    //  logger.info("Copying " + source + " to " + destination)
    //  clientForRemoteCommand.executeCommand("cp -r " + source + " " + destination)
    //  clientForRemoteCommand.close
    // }
    //
    //
    // def deploy(list: ArrayList[File], deployTmpDir: String) = {
    //  val remoteDeployDir = props("remoteWebStartDeployDir") + "lib/"
    //  val listOfSignedFiles = list.toArray.map{ a => deployTmpDir + a.toString.split("/").last }
    //  val clientForRemoteCommand = ssh.openSessionChannel
    //  clientForRemoteCommand.executeCommand("mkdir -p " + remoteDeployDir)  // make sure that directories exist
    //  clientForRemoteCommand.close
    //  backup
    //
    //  def actionBlock(localFile: String): Unit = {
    //    val clientForRemoteCommand = ssh.openSessionChannel
    //    val comparator = new JarEntryComparator
    //    var listOfCRCLocalFile = comparator.loadAndThrowListOfCrcs(localFile) // XXX: variable
    //    clientForRemoteCommand.executeCommand(
    //      props("remoteProjectutilsDir") + "getcrcs" + " " +
    //      remoteDeployDir + localFile.split("/").last + " " +
    //      props("remoteScalaBin"))
    //    val input = new BufferedReader(new InputStreamReader(clientForRemoteCommand.getInputStream))
    //    var output = "" // XXX: variable
    //    var line = "" // XXX: variable
    //    while (line != null) {
    //      output += line
    //      line = input.readLine
    //    }
    //    input.close
    //
    //    var out = List[String]() // XXX: variable
    //    output.split(",").foreach{ a => out ++= List[String](a) }
    //    logger.debug("1: " + out)
    //    logger.debug("2: " + listOfCRCLocalFile)
    //    logger.debug("result: " + (listOfCRCLocalFile -- out))
    //    if ((out -- listOfCRCLocalFile) == List()) {
    //      logger.info("FILE IDENTICAL: " + localFile.split("/").last)
    //    } else {
    //      logger.warn("FILE DIFFERENT: " + localFile.split("/").last)
    //      logger.info("Uploading " + localFile.split("/").last)
    //      putLocalFileToRemoteHost(localFile, remoteDeployDir + localFile.split("/").last)
    //    }
    //    clientForRemoteCommand.close
    //  }
    //  logger.info("Deploying")
    //  listOfSignedFiles foreach {
    //    actionBlock(_)
    //  }
    //  logger.info("Verifying deploy")
    //  listOfSignedFiles foreach {
    //    actionBlock(_)
    //  }
    //  // deploying jnlp file
    //  logger.info("Generating JNLP file")
    //  var arguments = "" // XXX: variable
    //  for( i <- props("webstartArgumentsJVM")) { // XXX: maybe switch to normal String instead of List[String]
    //    arguments += i + " "
    //  }
    //  val jnlp = new JNLPSkeleton(
    //    props("jnlpMainClass") getOrElse "com.yourapp.Main",
    //    props("jnlpAppName") getOrElse "MyAppName",
    //    props("jnlpCodebase") getOrElse "http://127.0.0.1:8080/MyAppName",
    //    props("jnlpFileName") getOrElse "start.jnlp",
    //    (props(("deployFilesBasic").get) ++ (props("deployFilesAdditionalDependencies").get)),
    //    arguments,
    //    props("jnlpVendor") getOrElse "My Organization",
    //    props("jnlpHomePage") getOrElse "http://home.page/",
    //    props("jnlpIcon") getOrElse "/icon.ico",
    //    props("jnlpDescription") getOrElse "My App Description"
    //    )
    //  val tempJnlpFileName = "/tmp/launch-" + Deployer.uuid + ".jnlp"
    //  logger.debug("Temporary jnlp filename: " + tempJnlpFileName)
    //  jnlp.saveJNLP(tempJnlpFileName)
    //  logger.warn("Putting JNLP file to remote server")
    //  putLocalFileToRemoteHost(tempJnlpFileName, props("remoteWebStartDeployDir") + props("jnlpFileName") )
    // }


    def putLocalFileToRemoteHost(source: String, destination: String) = {
        val client = ssh.openSftpClient
        logger.debug("Source: " + source + " to destination: " + destination + " on connection: " + client)
        client.put(source, destination)
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
            ssh.connect(host, port.toInt)
        } catch {
            case x: UnknownHostException =>
                logger.error("Couldn't connect to remote host: " + host + " at port " + port)
                exit
        }


    def disconnect = ssh.disconnect

}
