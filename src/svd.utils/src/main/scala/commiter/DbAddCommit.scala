// // © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// // This Software is a close code project. You may not redistribute this code without permission of author.
// 
// package com.verknowsys.served.utils.commiter
// 
// 
// import com.verknowsys.served.utils.command.exec.CommandExec
// import com.verknowsys.served.utils.Utils
// 
// import org.apache.log4j.{Logger}
// import org.neodatis.odb.{ODBFactory, ODB}
// 
// 
// object DbAddCommit extends Utils {
// 
//  // override
//  // def logger = Logger.getLogger(DbAddCommit.getClass)
//  lazy val debug = props("debug")
//  lazy val gitRepositoryProjectDir = props("gitRepositoryProjectDir")
//  lazy val databaseName = System.getProperty("user.home") + "/" + ".svd/" + props("xmppDatabaseFileName")
//  lazy val git = props("gitExecutable")
//  
//  def writeCommitToDataBase(arg: Commit) = {
//    var odb: ODB = null
//    try {
//      odb = ODBFactory.openClient(props("xmppDatabaseListenAddress"), props("databaseODBPort"), props("xmppDatabaseName"))
//      odb.store( arg )
//      odb.commit
//    } catch {
//      case x: Throwable => {
//        logger.info("### Warning: There were problems while connecting to remote ODB server."+
//            " Will try to write directly to ODB file")
//        odb = ODBFactory.open(databaseName)
//        odb.store( arg )
//        odb.commit
//      }
//    } finally {
//      if (odb != null) { 
//        odb.close
//      } 
//    }
//  }
// 
//  def doError {
//    logger.error("### Error: bad arguments.\nUsage: " + getClass.getName + " config-file sha1-start sha1-end")
//    exit(1)
//  }
// 
//  /**
//   * args(0) -> name of config file to be used
//   * args(1) -> commit sha (beginning of range)
//   * args(2) -> commit sha (end of range)
//   */
//  def main(args: Array[String]) {
//    // initLogger
//    if (args.length == 0) doError
//    props = new Preferences(args(0)) // initialize preferences based on given argument (project config file)
//    try {
//      val command = Array(git, "--git-dir=" + gitRepositoryProjectDir, "rev-list", args(1) + "..." + args(2))
//      logger.debug("*** performing action: \"" + command.map{ a => a + ", "}.mkString + "\"")
//      val listOfSha1 = List.fromString(CommandExec.cmdExec(command), '\n')
//      listOfSha1.foreach { oneOf =>
//        val commit = new Commit(oneOf)
//        writeCommitToDataBase( commit ) // sha1, show?
//        logger.info("Adding commit: sha1: " + commit.commitSha1 + " commited at: " + commit.date)
//      }
//      logger.info("Commited. Done")
//      exit(0)
//    } catch {
//      case x: Throwable => {
//        logger.debug("StackTrace:\n" + x)
//        doError
//      }
//    } 
//  }
// }