// // © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// // This Software is a close code project. You may not redistribute this code without permission of author.
// 
// package com.verknowsys.served.utils.deployer
// 
// 
// import com.verknowsys.served.utils.skeletons.JNLPSkeleton
// import com.verknowsys.served.utils.ssh.utils.SSHCommand
// import com.verknowsys.served.utils.SvdUtils
// import com.verknowsys.served.utils.signals.{Init, Quit}
// import com.verknowsys.served.utils.command.exec.CommandExec
// import com.verknowsys.served.utils.jar.comparator.JarEntryComparator
// 
// import actors._
// import java.io._
// import java.util.{UUID, Date, ArrayList}
// import org.apache.commons.io.{FileUtils, CopyUtils}
// import org.apache.log4j.{ConsoleAppender, Level, PatternLayout, Logger}
// import java.util.regex.{Matcher, Pattern}
// import java.lang.String
// 
// /**
//  * User: dmilith
//  * Date: Jun 26, 2009
//  * Time: 12:04:35 PM
//  */
// 
// 
// object Deployer extends SvdUtilsCommon {
// 
//  val uuid = UUID.randomUUID.toString
//  Logger.getLogger("com.sshtools").setLevel(Level.WARN) // should quiet too verbose messages of sshtools
// 
// 
//  def main(args: Array[String]) {
// 
//    lazy val filesToBeDeployed = new ArrayList[File]()
//    lazy val deployTmpDir = "/tmp/deployer-" + uuid + "/"
//    lazy val pathToMaven2Repo = System.getProperty("user.home") + "/.m2/repository/"
// 
//    addShutdownHook {
//      debug("Into shutdown hook")
//      info("Finished.")
//    }
// 
//    if (args.size == 0) {
//      error("Missing argument: (config filename)")
//      exit
//    }
// 
//    props = new Preferences(args(0))
//    val debug = props("debug")
//    if (debug) {
//      setLoggerLevelDebug(Level.TRACE)
//    }
// 
//    lazy val basic_jar_names = props("deployFilesBasic")
//    lazy val dependency_jar_names = props("deployFilesAdditionalDependencies")
//    lazy val codebaseLocalDir = System.getProperty("user.home") + "/" + props("directoryForLocalDeploy")
//    var macAppDeploy = false
//    var basicOnly_? = props("deployOnlyBasicFiles")
// 
//    info("Starting Deployer.. v1.1")
//    info("Deployer mode: " + (if (debug) "DEBUG" else "NORMAL"))
//    info("Deployer home dir: " + System.getProperty("user.home") + "/.svd/")
//    info("Maven 2 Repository dir: " + pathToMaven2Repo)
//    info("Deploy tmp dir: " + deployTmpDir)
//    info("Will deploy files to remote host to: " + props("remoteWebStartDeployDir"))
//    info("Given arguments: " + args.map{ a => if (a == args.last) a + ". " else a + ", " }.mkString)
// 
//    def remoteSSHDeploy = {
//      SSHCommand.auth
//      debug("Performing tasks with given list of files: " + filesToBeDeployed.toArray.map {a => deployTmpDir + a.toString.split("/").last})
//      debug("Remote dir containing jars: " + props("remoteWebStartDeployDir") + "lib/")
//      SSHCommand.deploy(filesToBeDeployed, deployTmpDir)
//    }
// 
//    // check given arguments
//    if (args.size == 1) {
//      warn("Requested to perform remote deploy")
//      // normal deploy based on config values
//      SSHCommand.connect
//      getFilesFromMavenRepositoryAndSignThem
//      remoteSSHDeploy
//    } else {
//      if (args.size == 2) {
//        args(1) match {
//          case "mac-app" => {
//            warn("Requested to perform Mac Application deploy")
//            basicOnly_? = false
//            macAppDeploy = true
//            getFilesFromMavenRepositoryAndSignThem
//            deployLocal
//            copyAndInstallMacApp
//            // TODO: implement Mac Application deploy support
//          }
//          case "windows-app" => {
//            warn("Requested to perform Windows Application deploy")
//            basicOnly_? = false
//            error("Not yet implemented!")
//            // TODO: implement deployment of windows application
//          }
//          case "linux-app" => {
//            warn("Requested to perform Linux Application deploy")
//            basicOnly_? = false
//            error("Not yet implemented!")
//            // TODO: implement deployment of linux application
//          }
//          case "local-full" => {
//            // local full deploy without ssh actor
//            warn("Requested to perform FULL local deploy")
//            basicOnly_? = false
//            getFilesFromMavenRepositoryAndSignThem
//            deployLocal
//          }
//          case "full" => {
//            // remote full deploy without ssh actor
//            warn("Requested to perform FULL remote deploy")
//            basicOnly_? = false
//            SSHCommand.connect
//            getFilesFromMavenRepositoryAndSignThem
//            remoteSSHDeploy
//          }
//          case "local" => {
//            // local deploy without ssh actor
//            warn("Requested to perform local deploy")
//            getFilesFromMavenRepositoryAndSignThem
//            deployLocal
//          }
//          case _ =>
//            deployerHelp
//        }
//      }
//    }
// 
//    def getFilesFromMavenRepositoryAndSignThem = {
//      info("Searching for jars in Maven repository..")
//      (basic_jar_names ++ (if (!basicOnly_?) dependency_jar_names else Nil)).foreach {
//        file =>
//            debug("FileName: *" + file + "*" + " <- " + basic_jar_names)
//            debug("Basic Jar names contains given file? " + basic_jar_names.contains(file))
//            findFile(new File(pathToMaven2Repo + (if (basic_jar_names.contains(file)) props("projectGroupId") else "")), new P { // NOTE: 2009-10-19 03:04:00 - dmilith - projectGroupId gives huge boost in deployment time
//              override
//              def accept(t: String): Boolean = {
//                val fileRegex = ".*" + file + "$"
//                val pattern = Pattern.compile(fileRegex)
//                val mat = pattern.matcher(t)
//                if (mat.find) {
//                  if (!macAppDeploy)
//                    signJar(t)
//                  else { // don't sign jars when it's mac-app deploy
//                    info("Copying jar: " + t.split("/").last)
//                    FileUtils.copyFileToDirectory(new File(t), new File(deployTmpDir)) // copy files to temporary dir
//                  }
//                  return true
//                }
//                return false
//              }
//            }, filesToBeDeployed)
//      }
//    }
// 
// 
//    def signJar(fileToBeSigned: String) = {
//      new File(deployTmpDir).mkdir // make temporary place for jars before signinig
//      info("Signing and copying jar: " + fileToBeSigned.split("/").last)
//      FileUtils.copyFileToDirectory(new File(fileToBeSigned), new File(deployTmpDir)) // copy files to temporary dir
//      val signCommand = Array(
//        props("jarSignerExecutable"), "-storepass", props("jarSignerPassword"),
//        deployTmpDir + fileToBeSigned.split("/").last, props("jarSignerKeyName")
//        )
//      debug(CommandExec.cmdExec(signCommand).trim)
//    }
// 
// 
//    def backupLocal = {
//      val backupDate = (new Date).toString.replaceAll(" |:", "_")
//      val backupDir = new File(codebaseLocalDir + "../OLD_DEPLOYS/OLD_LOCAL_DEPLOY_" + backupDate + "/")
//      val localDeployDir = new File(codebaseLocalDir)
//      backupDir.mkdir
//      localDeployDir.mkdir
//      info("Copying files from " + localDeployDir + " to " + backupDir)
//      FileUtils.copyDirectory(localDeployDir, backupDir)
//    }
// 
// 
//    def deployLocal = {
//      backupLocal
//      debug("Files to be deployed: " + filesToBeDeployed.toArray.map{ a => a + ", " }.mkString)
//      filesToBeDeployed.toArray foreach {
//        localFile => {
//          val comparator = new JarEntryComparator
//          comparator.load(deployTmpDir + localFile.toString.split("/").last, codebaseLocalDir + "lib/" + localFile.toString.split("/").last)
//          debug("COMPARATOR: " + comparator + ", -- diff?: " + comparator.diff_?)
//          if (comparator.diff_?) {
//            warn("File DIFFERENT: " + localFile.toString.split("/").last)
//            FileUtils.copyFileToDirectory(new File(deployTmpDir + localFile.toString.split("/").last), new File(codebaseLocalDir + "lib/"))
//          } else {
//            info("File IDENTICAL: " + localFile.toString.split("/").last)
//          }
//        }
//      }
//      if (!macAppDeploy) {
//        warn("Generating JNLP file")
//        val arguments = props("webstartArgumentsJVM").map{ a => a + " " }.mkString
//        val jnlp = new JNLPSkeleton(
//          props("jnlpMainClass"),
//          props("jnlpAppName"),
//          "file://" + codebaseLocalDir,
//          props("jnlpFileName"),
//          (props("deployFilesBasic") ++ props("deployFilesAdditionalDependencies")),
//          arguments,
//          props("jnlpVendor"),
//          "file://" + codebaseLocalDir,
//          props("jnlpIcon"),
//          props("jnlpDescription")
//          )
//        val tempJnlpFileName = "/tmp/" + props("jnlpFileName")
//        jnlp.saveJNLP(tempJnlpFileName)
//        info("Putting JNLP file to local deploy dir")
//        FileUtils.copyFileToDirectory(new File(tempJnlpFileName), new File(codebaseLocalDir))
//      }
//    }
// 
// 
//    def copyAndInstallMacApp = {
//      info("Copying jars to temporary mac app")
//      FileUtils.copyDirectory(new File(deployTmpDir), new File(codebaseLocalDir + "../Coviob.app/lib")) // XXX: hardcoded Coviob.app name
//      // XXX: application skeleton must be uploaded to ~/.svd before deploy
//      info("Copying Application to /Applications")
//      FileUtils.copyDirectory(new File(codebaseLocalDir + "../Coviob.app"), new File("/Applications/Coviob.app"))
//      info("Making executable of app starting script..")
//      CommandExec.cmdExec(Array("chmod", "777", "/Applications/Coviob.app/Contents/MacOS/coviob2")) // XXX: why the fuck copying files will result changing permissions to files?!
//    }
// 
// 
//    def deployerHelp = warn("\n\nDeployer quick help:\nValid params:\n" +
//        "\tlocal -> for basic local deploy\n" +
//        "\tlocal-full -> for full local deploy\n" +
//        "\twindows-app -> for Windows application deploy\n" +
//        "\tlinux-app -> for Linux (distro independent) application deploy\n" +
//        "\tmac-app -> for Mac OS X application deploy and install\n" +
//        "\tfull -> for full remote deploy\n" +
//        "\tTo run remote deploy, no arguments (defaults based on xml config).")
// 
//     exit
//  }
// 
// }