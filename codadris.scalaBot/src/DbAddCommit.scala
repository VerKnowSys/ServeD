// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package commiter

import scalabot._
import scala.actors._
import java.io._
import org.neodatis.odb._
// import org.neodatis.odb.Configuration


object DbAddCommit {
	
	private val prefs = (new Preferences).loadPreferences
	private val debug = prefs.getb("debug")
	private val repositoryDir = prefs.get("repositoryDir")
	
	def writeCommitToDataBase(arg: Commit) = {
		var odb: ODB = null
		try {
			odb = ODBFactory.openClient("127.0.0.1",50603,"commitDatabase") // TODO: add preferences params instead of hardcoded vals
			odb.store( arg )
			odb.commit
		} catch {
			case x: Throwable => {
				println("### Error: There were problems while storing data in database")
				if (debug) println(x.printStackTrace)
			}
		} finally {
			if (odb != null) { 
				odb.close
			} 
		}
	}
	
	def main(args: Array[String]) = {
		// Configuration.useMultiThread(true, 5)
		// Configuration.setDatabaseCharacterEncoding("UTF-8")
		try{
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
				println("### Error: bad arguments.\nUsage: scriptname sha1-start sha1-end")
				if (debug) x.printStackTrace
				exit(1)
			}
		} 
	}
}