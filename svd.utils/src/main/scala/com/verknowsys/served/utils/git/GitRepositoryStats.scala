package com.verknowsys.served.utils.git

/**
 * Some statistics for git repository
 *
 * @author teamon
 */
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
                val label = "%s [%s]".format(commit.author.name, commit.author.email)
                if (authors.contains(label)) authors(label) += 1
                else authors(label) = 1
        }
    
        authors.toMap
    }
}