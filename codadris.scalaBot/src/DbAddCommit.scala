// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.

package scalabot

import scala.actors._
import java.io._
import org.neodatis.odb._


object DbAddCommit {
	
	val debug = ScalaBot.debug
	
	def writeCommitToDataBase(args: Map[String,Boolean]) = {
		val odb = ODBFactory.open("../ScalaBotCommitDataBase.neodatis")
		args.foreach{ oneOf => 
			odb.store( oneOf )
		}
			// writing Sha1 to odb
		// query.addExtension("datanucleus.query.flushBeforeExecution","true");
		odb.close
	}
	
	def main(args: Array[String]) = {
		println("*** Adding new commit list to objDb: " + args(0) + " to " + args(1))
		// git-rev-list
		val command = Array("git", "rev-list", args(0) + "..." + args(1))
		val output: String = CommandExec.cmdExec(command)
		var listOfSha1 = List.fromString(output, ' ')
		println(listOfSha1)
		
		listOfSha1.foreach{ oneOf => 
			val x: Map[String,Boolean] = Map( oneOf -> true )
			writeCommitToDataBase( x )	// sha1, show?
		}
			
		// val inputStream = p.getInputStream
		// val brCleanUp = new BufferedReader(new InputStreamReader (inputStream))

		println("Done")
		
	}

}