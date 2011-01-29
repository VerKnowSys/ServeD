package com.verknowsys.served.utils.git

import com.verknowsys.served.utils._

import scala.collection.mutable.Map
import scala.collection.JavaConversions._
import org.eclipse.jgit.api._
import org.eclipse.jgit.api.errors.JGitInternalException
import org.eclipse.jgit.api.errors.InvalidConfigurationException
import org.eclipse.jgit.lib.{AnyObjectId, ObjectId, PersonIdent, Constants, ProgressMonitor}
import org.eclipse.jgit.lib.TextProgressMonitor
import org.eclipse.jgit.storage.file.FileRepository
import org.eclipse.jgit.revwalk.{RevCommit, RevWalk}
import org.eclipse.jgit.transport.{RemoteConfig, Transport, RefSpec, URIish}

import java.io.File
import java.util.Date

import akka.util.Logging

// NOTE: Jgit JavaDoc at http://s.teamon.eu/jgit-doc/
// NOTE: Jgit User guide at http://wiki.eclipse.org/JGit/User_Guide


/**
 * JGit RevCommit wrapper for more scala-like syntax
 *
 * @author teamon
 **/
class Commit(val origin: RevCommit) {
    /**
     * Returns commit`s date
     *
     * @author teamon
     */
    def date = new Date(origin.getCommitTime.toLong * 1000)

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
 **/
class Author(val origin: PersonIdent) {
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

object GitRepository {
    /**
     * Create git repository for specified directory
     * @author teamon
    */
    def init(dir: String, bare: Boolean = false) = {
        Git.init().setDirectory(dir).setBare(bare).call
        new GitRepository(dir) // XXX: Handle Caused by: java.lang.IllegalStateException: Repository already exists:
    }
    
    /**
     * Clone git repository to specified directory
     * clone is init + add remote + pull
     * 
     * @author teamon
    */
    def clone(dir: String, remote: String) = {
        val repo = init(dir)
        repo.addRemote("origin", remote)
        repo.pull
        repo
    }
    
    /**
     * List git repositories for specified directory
     * @author teamon
     */
    def list(dir: String): List[GitRepository] = {
        val list = new File(dir).list
        if(list == null) List()
        else list.toList.map { new GitRepository(_) }
    }

}

/**
 * GitRepository class  wrapper for more scala-like syntax
 *
 * @author teamon
 */
class GitRepository(val dir: String) extends Logging {
    val dotgit = new File(dir, ".git")
    val directory = if(dotgit.exists) dotgit else new File(dir)
    val gitRepo = new FileRepository(directory)

    lazy val git = new Git(gitRepo)

    // lazy val (headPath, headFile) = if(isBare) (dir + "/refs/heads", "master") else (dir + "/.git/logs", "HEAD")
    
    def isBare = gitRepo.isBare
    
    /** 
     * Return git repository name (actually it is the name of directory)
     * 
     * @author teamon
     */
    def name = new File(dir).getName
    
    /** 
     * Return git repository name path
     * 
     * @author teamon
     */
    def path = gitRepo.getWorkTree.getPath
    
    /**
     * Returns git ref for HEAD
     *
     * @author teamon
     */
    def head = gitRepo.getRef("HEAD")
    
    /**
     * Returns current branch name
     *
     * @author teamon
     */
    def currentBranch = gitRepo.getBranch
    
    
    // Basic git commands (close to command line)
    
    
    /** 
     * Same as `git add [path]`
     *
     * {{{
     * repo.add("README") // relative path
     * }}}
     * 
     * @param path path added to git
     * @author teamon
     */
    def add(path: String) = git.add.addFilepattern(path).call

    /** 
     * Same as `git commit -m [message]`
     *
     * {{{
     * repo.commit("First commit")
     * }}}
     * 
     * @param message commit message
     * @author teamon
     */
    def commit(message: String) = git.commit.setMessage(message).call
    
    
    /** 
     * Same as `git branch [name]`
     *
     * {{{
     * repo.branch("newbranch")
     * }}}
     * 
     * @param name name of new branch
     * @author teamon
     */
    def branch(name: String) = git.branchCreate.setName(name).call

    /**
     * Returns commit history iterator
     *
     * @author teamon
     */
    def history: Iterator[RevCommit] = git.log.call.iterator
    
    /**
     * Returns commit history for specified range
     * @example
     * repo.history ( " 0261cf4d9283d1fdd45c27518a16a0d18ed231cc ", " 07ac043347fb979b5374ec6fdd064bcc246f005b " ).foreach { commit =>
     *      	println(commit.sha)
     * 			println(commit.date)
     * 			println(commit.author.nameAndEmail)
     * 			println(commit.message)
     * }
     *
     * @author teamon
     */
    def history(from: AnyObjectId, to: AnyObjectId = head): Iterator[RevCommit] = git.log.addRange(from, to).call.iterator

    
    def remotes = RemoteConfig.getAllRemoteConfigs(gitRepo.getConfig).toList

    /**
     * Adds remote to repository
     *
     * {{{
     * repo.addRemote("origin", "/path/to/remote/repo.git")
     * }}}
     *
     * @author teamon
     **/
    def addRemote(name: String, uri: String) {
        val config = gitRepo.getConfig
        config.setString("branch", "master", "remote", "origin")
        config.setString("branch", "master", "merge", "refs/heads/master")
                
        val remoteConfig = new RemoteConfig(config, name)
        remoteConfig.addURI(new URIish(uri))
        remoteConfig.addFetchRefSpec(new RefSpec("+refs/heads/*:refs/remotes/origin/*"));
        remoteConfig.update(config)
        config.save
    }

    /**
     * Performs 'git pull' on repository
     *
     * @author teamon
     */
    def pull =  git.pull.call

    /**
     * Performs 'git push' on repository
     *
     * @author teamon
     */
    def push(remote: String = Constants.DEFAULT_REMOTE_NAME, ref: String = currentBranch) = git.push.setRemote(remote).setRefSpecs(new RefSpec(ref)).call
}


class GitRepositoryStats(repo: GitRepository){
    /**
     * Returns map of authors: (Author`s name and email -> number of commits)
     *
     * @author teamon
     */
    def authors = {
        // TODO: Reimplement this using fold/reduce/collect/whatever, mutable map is lame
        val authors = Map[String, Int]()
    
        repo.history.foreach {
            commit =>
                val label = commit.author.nameAndEmail
                if (authors.contains(label)) authors(label) += 1
                else authors(label) = 1
        }
    
        authors.toMap
    }
}
