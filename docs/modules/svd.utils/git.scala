/**
 * **svd.utils.git** includes API for manipulating git repositories
 * 
 */
 
// ### Package and necessary imports
import com.verknowsys.served.utils.git._

// ### Creating git repository

// Create new normal repository (`git init`)
val repo = Git.init("/path/to/repo")

// Create new bare repository (`git init --bare`)
val repo = Git.init("/path/to/bare/repo.git", bare = true)

// Clone repository (`git clone`)
val repo = Git.clone("/path/to/repo", "remote_repo_uri.git")

// ### Getting information about repository

// Basic information
val repo = new GitRepository("/path/to/repo")
repo.name     // name of repository (the last part of path)
repo.path     // full path
repo.isBare   // check if repository was created with `--bare` option

// HEAD reference
repo.head

// ### Making changes

// Commit file (`git add`, `git commit -m`)
repo.add("README")  // add file to index
repo.commit("Added README")

// Provide author and committer
val someone = new Author("J. Author", "a@example.com")
val me = new Author("J. Committer","c@example.com")
repo.commit("Some changes", author = someone, committer = me)

// ### Branches
// Current branch name
repo.currentBranch

// Branch list
repo.branchList

// Create new branch (`git branch`)
repo.branch("feature")

// Checkout branch (`git checkout`)
repo.checkout("master")


// ### Commits
// Full history (`git log`)
repo.history
repo.history.take(10) // last 10 commits

// Partial history
repo.history("fe134..123413") // from provided sha to HEAD
repo.history("fe134..123413", "efda3413...4314") // commits between this two refs

// Commit details
val commit = repo.history.next // first commit form history
commit.date
commit.sha
commit.message
commit.author    // Author object
commit.committer // Author object

// Author object
author.name
author.email


// ### Remote
// List remotes
repo.remotes

// Add remote (`git remote add`)
repo.addRemote("origin", "path/to/remote")

// Push changes to remote (`git push`)
repo.push

// Pull changes from remote (`git pull`)
repo.pull
  
 
// ### Other

// List repositories in directory
Git.list("some/directory") // List[GitRepository]
  
  
// ### GitStats - various statistics for repository

// Get all commits grouped by author
val stats = new GitStats(repo)
stats.authors



/**
 * 
 * **svd.utils.git** is part of **ServeD** project. 
 * 
 * © Copyright 2009-2011 VerKnowSys
 *
 * Daniel Dettlaff and Tymon Tobolski
 * 
 * This Software is a close code project. You may not redistribute this code or documentation without permission of author.
 * 
 */
