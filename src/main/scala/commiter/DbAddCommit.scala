// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package commiter

import command.exec.CommandExec
import java.io.OutputStreamWriter
import org.apache.log4j.{ConsoleAppender, Level, PatternLayout, Logger}
import org.neodatis.odb.{ODBFactory, ODB}
import prefs.Preferences
import utils.Utils


object DbAddCommit extends Utils {

	private val logger = Logger.getLogger(DbAddCommit.getClass)
	private val prefs = new Preferences       // TODO: add custom config file support
	private val debug = prefs.getb("debug")
	private val gitRepositoryProjectDir = prefs.get("gitRepositoryProjectDir")
	private val databaseName = System.getProperty("user.home") + "/" + ".codadris/" + prefs.get("xmppDatabaseFileName")
	private val git = prefs.get("gitExecutable")
	
	def writeCommitToDataBase(arg: Commit) = {
		var odb: ODB = null
		try {
			odb = ODBFactory.openClient(prefs.get("xmppDatabaseListenAddress"), prefs.geti("databaseODBPort"), prefs.get("xmppDatabaseName"))
			odb.store( arg )
			odb.commit
		} catch {
			case x: Throwable => {
				logger.info("### Warning: There were problems while connecting to remote ODB server."+
						" Will try to write directly to ODB file")
				odb = ODBFactory.open(databaseName)
				odb.store( arg )
				odb.commit
			}
		} finally {
			if (odb != null) { 
				odb.close
			} 
		}
	}

	/**
	 * args(0) -> commit sha (beginning of range)
	 * args(1) -> commit sha (end of range)
	 */
	def main(args: Array[String]) {
		initLogger
		try {
			val command = Array(git, "--git-dir=" + gitRepositoryProjectDir, "rev-list", args(0) + "..." + args(1))
			logger.debug("*** performing " + command.map{ a => a })
			val listOfSha1 = List.fromString(CommandExec.cmdExec(command), '\n')
			listOfSha1.foreach { oneOf =>
				val commit = new Commit(oneOf)
				writeCommitToDataBase( commit )	// sha1, show?
				logger.debug("*** writeCommitToDatabase: " + oneOf )
			}
			logger.info("Commited. Done")
			exit(0)
		} catch {
			case x: Throwable => {
				logger.info("### Error: bad arguments.\nUsage: scriptname sha1-start sha1-end")
				if (debug) x.printStackTrace
				exit(1)
			}
		} 
	}
}