package com.verknowsys.served.git

import scala.collection.mutable.Map
import org.eclipse.jgit.api._
import org.eclipse.jgit.lib.{AnyObjectId, ObjectId, PersonIdent, Constants, Repository => JgitRepository}
import org.eclipse.jgit.revwalk.{RevCommit, RevWalk}
import scala.collection.JavaConversions._
import java.io.File
import java.util.Date

// XXX: http://wiki.eclipse.org/JGit/User_Guide - last paragraph

object Git {
	def main(args: Array[String]): Unit = {
		val repo = new Repository("/Users/teamon/code/verknowsys/ServeD")
		println()
		println("com.verknowsys.served.git")
		
		println("---> repo.authors")
		println(repo.authors)
		println()
		
		println("---> repo.history")
		println(repo.history.toList.size)
		
		repo.history("0261cf4d9283d1fdd45c27518a16a0d18ed231cc", "07ac043347fb979b5374ec6fdd064bcc246f005b").foreach {  c => 
			println(c.sha)
			println(c.date)
			println(c.author.nameAndEmail)
			println(c.message)
			println()
		}
	}
}

import Git._

/**
 * Proxy classes for more scala-like syntax
 *
 * @author teamon
 */
class Commit(val origin: RevCommit) {
	def date = new Date(origin.getCommitTime)
	def sha = origin.getName
	def message = origin.getFullMessage
	def author = origin.getAuthorIdent
}

class Author(val origin: PersonIdent){
	def name = origin.getName
	def email = origin.getEmailAddress
	def nameAndEmail = "%s [%s]".format(name, email)
}

class Repository(dir: String) {
	lazy val gitRepo = new JgitRepository(new File(dir, ".git"))
	lazy val git = new Git(gitRepo)

	/**
	 * Returns current branch name
	 *
	 * @author teamon
	 */
	def currentBranch = gitRepo.getBranch


	/**
	 * Returns commit history
	 *
	 * @author teamon
	 */	
	def history = {
		val rw = new RevWalk(gitRepo)
		rw.markStart(rw.lookupCommit(gitRepo.mapCommit(Constants.HEAD).getCommitId))
		rw
	}
	
	def history(from: AnyObjectId, to: AnyObjectId) = git.log.addRange(from, to).call
	
	/**
	 * Returns commit history
	 *
	 * @author teamon
	 */
	def authors = {
		val authors = Map[String, Int]()

		history.foreach { commit =>
			val label = commit.author.nameAndEmail
			if(authors.contains(label)) authors(label) += 1
			else authors(label) = 1
		}

		authors.toMap
	}

}

