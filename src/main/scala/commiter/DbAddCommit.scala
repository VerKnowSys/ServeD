// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package commiter

import org.neodatis.odb.{ODBFactory, ODB}
import scalabot._
import scala.actors._
import java.io._

object DbAddCommit {
	
	private var prefs: Preferences = null
	private var debug = true
	private var repositoryDir: String = null
	private var databaseName = ""
	
	def writeCommitToDataBase(arg: Commit) = {
		var odb: ODB = null
		try {
			odb = ODBFactory.openClient(prefs.get("ODBListenAddress"), prefs.geti("ODBPort"), prefs.get("ODBName"))
			odb.store( arg )
			odb.commit
		} catch {
			case x: Throwable => {
				println("### Error: There were problems while connecting to remote ODB server."+
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
	 * args(2) -> absolute path to git repository
	 */
	def main(args: Array[String]) = {
		try{
			prefs = new Preferences(args(2))
			debug = prefs.getb("debug")
			repositoryDir = prefs.get("repositoryDir")
			databaseName = args(2) + prefs.get("databaseName")
			
			val command = Array("git", "--git-dir=" + repositoryDir, "rev-list", args(0) + "..." + args(1))
			if (debug) println("*** performing "+command.map{ a => a })
			var listOfSha1 = List.fromString(CommandExec.cmdExec(command), '\n')
			listOfSha1.foreach { oneOf =>
				val commit = new Commit(oneOf)
				writeCommitToDataBase( commit )	// sha1, show?
				if (debug) println("*** writeCommitToDatabase: " + commit )
			}
			println("done. commit added")
			exit(0)
		} catch {
			case x: Throwable => {
				println("### Error: bad arguments.\nUsage: scriptname sha1-start sha1-end absolutePathToGitRepo")
				if (debug) x.printStackTrace
				exit(1)
			}
		} 
	}
}