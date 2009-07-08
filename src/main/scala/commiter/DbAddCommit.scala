// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package commiter

import command.exec.CommandExec
import org.apache.log4j.{ConsoleAppender, Level, PatternLayout, Logger}
import java.io.OutputStreamWriter


import org.neodatis.odb.{ODBFactory, ODB}
import prefs.Preferences
import scalabot._


object DbAddCommit {

	private val logger = Logger.getLogger(DbAddCommit.getClass)
	private var prefs: Preferences = null
	private var debug = true
	private var gitRepositoryProjectDir: String = null
	private var databaseName = ""
	private var git = ""
	
	def writeCommitToDataBase(arg: Commit) = {
		var odb: ODB = null
		try {
			odb = ODBFactory.openClient(prefs.get("xmppDatabaseListenAddress"), prefs.geti("xmppDatabaseODBPort"), prefs.get("xmppDatabaseName"))
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

	def initLogger = {
		val appender = new ConsoleAppender
		appender.setName(ConsoleAppender.SYSTEM_OUT);
		appender.setWriter(new OutputStreamWriter(System.out))
		val level = if (debug) Level.TRACE else Level.WARN
		appender.setThreshold(level)
		appender.setLayout(new PatternLayout("{ %-5p %d : %m }%n"));
		Logger.getRootLogger.addAppender(appender)
	}

	/**
	 * args(0) -> commit sha (beginning of range)
	 * args(1) -> commit sha (end of range)
	 * args(2) -> absolute path to git repository
	 */
	def main(args: Array[String]) {
		initLogger
		try{
			prefs = (new Preferences).loadPreferences
			debug = prefs.getb("debug")
			gitRepositoryProjectDir = prefs.get("gitRepositoryProjectDir")
			databaseName = System.getProperty("user.home") + "/" + ".codadris/" + prefs.get("xmppDatabaseFileName")
			git = prefs.get("gitExecutable")

			val command = Array(git, "--git-dir=" + gitRepositoryProjectDir, "rev-list", args(0) + "..." + args(1))
			logger.debug("*** performing "+command.map{ a => a })
			var listOfSha1 = List.fromString(CommandExec.cmdExec(command), '\n')
			listOfSha1.foreach { oneOf =>
				val commit = new Commit(oneOf)
				writeCommitToDataBase( commit )	// sha1, show?
				logger.debug("*** writeCommitToDatabase: " + commit )
				logger.info("commit added")
			}
			logger.info("done")
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