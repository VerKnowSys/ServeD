/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.git

/**
 * Some statistics for git repository
 *
 * @author teamon
 */
class GitStats(repo: GitRepository){
    /**
     * Returns map of authors: (Author`s name and email -> number of commits)
     *
     * @author teamon
     */
    def authors = repo.history.toList.groupBy(_.author.name)
}
