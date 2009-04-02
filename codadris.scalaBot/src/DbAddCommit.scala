// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.

package scalabot

import scala.actors._
import java.util._
import java.io._
import org.neodatis.odb._


object DbAddCommit {
	
	val debug = ScalaBot.debug
	
	def writeCommitToDataBase(args: Array[String]) = {
		val commit = new Commit("Commit-From-Neodatis-Database-@" + new Date)
		// query.addExtension("datanucleus.query.flushBeforeExecution","true");
		val odb = ODBFactory.open("../ScalaBotCommitDataBase.neodatis")
		odb.store(commit.asInstanceOf[Commit])
		odb.close
	}
	
	def main(args: Array[String]) = {
		println("*** Adding new commit list to objDb: " + args(0) + " to " + args(1))
		// git-rev-list
		val command = Array("git", "rev-list", args(0) + "..." + args(1))
		println(CommandExec.cmdExec(command))
		
		// val inputStream = p.getInputStream
		// val brCleanUp = new BufferedReader(new InputStreamReader (inputStream))

		println("Done")
		
	}

}