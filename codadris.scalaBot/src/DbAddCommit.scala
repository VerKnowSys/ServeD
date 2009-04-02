// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.

package scalabot

import scala.actors._
import java.io._
import org.neodatis.odb._


object DbAddCommit {
	
	val debug = ScalaBot.debug
	
	def writeCommitToDataBase(arg: Map[String,Boolean]) = {
		val odb = ODBFactory.open("../ScalaBotCommitDataBase.neodatis")
		odb.store( arg )
		if (debug)
			println("*** writeCommitToDatabase: " + arg )
		// query.addExtension("datanucleus.query.flushBeforeExecution","true");
		odb.close
	}
	
	def main(args: Array[String]) = {
		if (debug)
			println("*** Reqested adding new commit list to objDb: " + args(0) + " to " + args(1))
		val command = Array("git", "rev-list", args(0) + "..." + args(1))
		val output: String = CommandExec.cmdExec(command)
		var listOfSha1 = List.fromString(output, '\n')
		listOfSha1.foreach{ oneOf => 
			val x: Map[String,Boolean] = Map( oneOf -> true )
			writeCommitToDataBase( x )	// sha1, show?
		}
		println("Done. All ok")
	}

}