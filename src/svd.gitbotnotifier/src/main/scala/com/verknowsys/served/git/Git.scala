package com.verknowsys.served.git

import scala.collection.mutable.Map
import org.eclipse.jgit.api._
import org.eclipse.jgit.lib.{AnyObjectId, ObjectId, PersonIdent, Constants, Repository => JgitRepository}
import org.eclipse.jgit.revwalk.{RevCommit, RevWalk}
import scala.collection.JavaConversions._
import java.io.File
import java.util.Date

// XXX: http://wiki.eclipse.org/JGit/User_Guide - last paragraph

/**
 * JGit RevCommit wrapper for more scala-like syntax
 *
 * @author teamon
 */
class Commit(val origin: RevCommit) {
	/**
	 * Returns commit`s date
	 * 
	 * @author teamon
	 */
	def date = new Date(origin.getCommitTime)
	
	/**
	 * Returns commit`s SHA1
	 * 
	 * @author teamon
	 */
	def sha = origin.getName
	
	/**
	 * Returns commit`s message
	 * 
	 * @author teamon
	 */
	def message = origin.getFullMessage
	
	/**
	 * Returns commit`s author
	 * 
	 * @author teamon
	 */
	def author = origin.getAuthorIdent
}

/**
 * JGit PersonIdent wrapper for more scala-like syntax
 *
 * @author teamon
 */
class Author(val origin: PersonIdent){
	/**
	 * Returns authors`s name
	 * 
	 * @author teamon
	 */
	def name = origin.getName
	
	/**
	 * Returns authors`s email
	 * 
	 * @author teamon
	 */
	def email = origin.getEmailAddress
	
	/**
	 * Returns authors`s name and email formatted as "name [email]"
	 * 
	 * @author teamon
	 */
	def nameAndEmail = "%s [%s]".format(name, email)
}

/**
 * GitRepository class  wrapper for more scala-like syntax
 *
 * @author teamon
 */
class GitRepository(dir: String) {
	lazy val gitRepo = new JgitRepository(new File(dir, ".git"))
	lazy val git = new Git(gitRepo)

	/**
	 * Returns current branch name
	 *
	 * @author teamon
	 */
	def currentBranch = gitRepo.getBranch


	/**
	 * Returns commit history (all commits)
	 *
	 * @author teamon
	 */	
	def history = {
		val rw = new RevWalk(gitRepo)
		rw.markStart(rw.lookupCommit(gitRepo.mapCommit(Constants.HEAD).getCommitId))
		rw
	}
	
	/**
	 * Returns commit history for specified range
	 *	@example
	 *	    repo.history("0261cf4d9283d1fdd45c27518a16a0d18ed231cc", "07ac043347fb979b5374ec6fdd064bcc246f005b").foreach { commit => 
	 *      	println(commit.sha)
	 *			println(commit.date)
	 *			println(commit.author.nameAndEmail)
	 *			println(commit.message)
	 *		}
	 *
	 * @author teamon
	 */
	def history(from: AnyObjectId, to: AnyObjectId) = git.log.addRange(from, to).call
	
	/**
	 * Returns map of authors: (Author`s name and email -> number of commits)
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

