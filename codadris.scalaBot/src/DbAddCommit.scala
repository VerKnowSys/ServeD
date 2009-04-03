// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package commiter

import scalabot._
import scala.actors._
import java.io._
import org.neodatis.odb._


object DbAddCommit {
	
	val debug = true
	val git_dir_of_repository = "/home/verknowsys/JAVA/codadris-public/"
	
	def writeCommitToDataBase(arg: Commit) = {
		val odb = ODBFactory.open(ScalaBot.databaseName)
		odb.store( arg )
		// query.addExtension("datanucleus.query.flushBeforeExecution","true");
		odb.close
	}
	
	def main(args: Array[String]) = {
		try{
			System.setProperty("user.dir", git_dir_of_repository)
			val command = Array("/usr/bin/git", "rev-list", args(0) + "..." + args(1))
			var listOfSha1 = List.fromString(CommandExec.cmdExec(command), '\n')
			listOfSha1.foreach { oneOf => 
				val commit = new Commit(oneOf)
				writeCommitToDataBase( commit )	// sha1, show?
				if (debug) println("*** writeCommitToDatabase: " + commit )
			}
			println("Done. All ok")
			exit(0)
		} catch {
			case x: Throwable => {
				println("### Error: bad arguments.\nUsage: scriptname sha1-start sha1-end")
				exit(1)
			}
		}
	}
}