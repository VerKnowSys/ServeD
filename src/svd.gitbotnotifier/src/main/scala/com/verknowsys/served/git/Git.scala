package com.verknowsys.served.git


import com.verknowsys.served.utils._

import scala.collection.mutable.Map
import scala.collection.JavaConversions._
import org.eclipse.jgit.api._
import org.eclipse.jgit.api.errors.JGitInternalException
import org.eclipse.jgit.lib.{AnyObjectId, ObjectId, PersonIdent, Constants, ProgressMonitor}
import org.eclipse.jgit.storage.file.FileRepository
import org.eclipse.jgit.revwalk.{RevCommit, RevWalk}
import org.eclipse.jgit.transport.RemoteConfig
import java.io.File
import java.util.Date

// NOTE: Jgit JavaDoc at http://s.teamon.eu/jgit-doc/


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
    def create(dir: String, bare: Boolean = false): GitRepository = {
        // XXX: Handle Caused by: java.lang.IllegalStateException: Repository already exists:
        val repo = new GitRepository(dir)
        repo.gitRepo.create(bare)
        repo
    }

}

/**
 * GitRepository class  wrapper for more scala-like syntax
 *
 * @author teamon
 */
class GitRepository(val dir: String) extends Utils {
    lazy val (gitRepo, isBare) = {
        val file = new File(dir, ".git")
        logger.trace("Git repository watch. Dir: %s, File: %s".format(dir, file))
        if(file.exists) {
            logger.trace("File exists. Loading normal repository")
            (new FileRepository(file), false)
        } else {
            logger.trace("File not exists. Loading bare repository")
            (new FileRepository(dir), true)
        }
    }
    
    lazy val git = new Git(gitRepo)

    lazy val headPath = if(isBare) dir + "/refs/heads/master" else dir + "/.git/logs/HEAD"

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
    def history = git.log.call
    
    /**
     * Returns commit history (all commits)
     *
     * @author teamon
     */
    def head = gitRepo.getRef("HEAD")

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
    def history(from: AnyObjectId, to: AnyObjectId = head) = git.log.addRange(from, to).call
    

    /**
     * Returns map of authors: (Author`s name and email -> number of commits)
     *
     * @author teamon
     */
    def authors = {
        val authors = Map[String, Int]()

        history.foreach {
            commit =>
                val label = commit.author.nameAndEmail
                if (authors.contains(label)) authors(label) += 1
                else authors(label) = 1
        }

        authors.toMap
    }

    /**
     * Adds remote to repository
     *
     * @example
     * val repo = GitRepository.create("/path/to/new/repo")
     *       repo.addRemote("origin", "/path/to/remote/repo.git")
     *       repo.pull
     *
     * @author teamon
     **/
    def addRemote(name: String, uri: String) {
        val config = gitRepo.getConfig
        val remoteConfig = new RemoteConfig(config, name)
        remoteConfig.addURI(uri)
        remoteConfig.update(config)
    }

    /**
     * Performs 'git pull' on repository
     *
     * In fact it does 'git fetch' followed by 'git pull FEATCH_HEADA'
     *
     * @author teamon
     */
    def pull {
        try {
            // TODO: Add progress monitor
            // val monitor = new ProgressMonitor {
            //     def beginTask(title: String, totalWorks: Int) {
            //         println("[pm] beginTask: %s, %d".format(title, totalWorks))
            //     }
            // 
            //     def endTask {
            //         println("[pm] endTask")
            //     }
            // 
            //     def start(totalTasks: Int) {
            //         println("[pm] start: %d".format(totalTasks))
            //     }
            // 
            //     def update(completed: Int) {
            //         println("[pm] update: %d".format(completed))
            //     }
            // 
            //     def isCancelled = false
            // }
            // 
            // val head = git.fetch.setProgressMonitor(monitor).call.getAdvertisedRef("HEAD")
            
            val head = git.fetch.call.getAdvertisedRef("HEAD")
            git.merge.include(head).call
        } catch {
            case e: JGitInternalException =>
                /// XXX Handle exception
                /// Caused by: org.eclipse.jgit.errors.TransportException: ssh://tunemates@git.verknowsys.com/git/ServeD.git: Auth fail
                println(e.getCause)
        }
    }
}
