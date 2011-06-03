package com.verknowsys.served.utils.git

import org.eclipse.jgit.revwalk.RevCommit
import java.util.Date

/**
 * JGit RevCommit wrapper for more scala-friendly syntax
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
    
    /**
     * Returns commit`s committer
     *
     * @author teamon
     */
    def committer = origin.getCommitterIdent
}