// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package commiter

import scalabot._
import scala.actors._
import java.io._
import org.neodatis.odb._


object DbAddCommit {
	
	val debug = ScalaBot.debug
	
	def writeCommitToDataBase(arg: Commit) = {
		val odb = ODBFactory.open(ScalaBot.databaseName)
		odb.store( arg )
		// query.addExtension("datanucleus.query.flushBeforeExecution","true");
		odb.close
	}
	
	def main(args: Array[String]) = {
		if (debug)
			println("*** Reqested adding new commit list to objDb: " + args(0) + " to " + args(1))
		val command = Array("git", "rev-list", args(0) + "..." + args(1))
		var listOfSha1 = List.fromString(CommandExec.cmdExec(command), '\n')
		listOfSha1.foreach{ oneOf => 
			val commit = new Commit(oneOf)
			writeCommitToDataBase( commit )	// sha1, show?
			if (debug)
				println("*** writeCommitToDatabase: " + commit )
		}
		println("Done. All ok")
	}

}