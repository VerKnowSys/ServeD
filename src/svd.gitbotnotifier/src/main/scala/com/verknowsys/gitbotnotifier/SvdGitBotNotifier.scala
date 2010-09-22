package com.verknowsys.served.gitbotnotifier

import scala.collection.JavaConversions._
import java.io.File
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.revwalk.filter.RevFilter
import org.eclipse.jgit.revwalk.{RevCommit, RevWalk}
import org.eclipse.jgit.lib.{PersonIdent, Constants, Repository}


class Repo(dir: String) {
  val repository = new Repository(new File(dir, ".git"))

   /**
   * Returns current branch name
   *
   * @author teamon
   */
  def branch = repository.getBranch

  
  /**
   * Return n last commits
   *
   * @param n Number of commits
   * @author teamon
   */
  def history(n: Int) = {
    val rw = new RevWalk(repository)
    rw.markStart(rw.lookupCommit(repository.mapCommit(Constants.HEAD).getCommitId))
    rw.take(n)
  }

}


/**
 * Proxy classes for more scala-like syntax
 *
 * @author teamon
 */
class Commit(val origin: RevCommit) {
  def message = origin.getFullMessage
  def author = origin.getAuthorIdent
}

class Author(val origin: PersonIdent){
  def name = origin.getName
  def email = origin.getEmailAddress
}


object SvdGitBotNotifier {
  implicit def RevCommitToCommit(c: RevCommit) = new Commit(c)
  implicit def PersonIdentToAuthor(c: PersonIdent) = new Author(c)
  
  def main(args: Array[String]){
    val repo = new Repo("path/to/repo")
    println(args(0))
    println(repo)
    println(repo.branch)

    println(repo.history(10).map(_.author.name))

  }
}


