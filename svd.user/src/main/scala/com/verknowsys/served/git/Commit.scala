/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.git

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
