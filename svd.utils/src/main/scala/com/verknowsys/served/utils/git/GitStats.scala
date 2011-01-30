package com.verknowsys.served.utils.git

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