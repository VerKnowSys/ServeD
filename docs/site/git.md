---
title: ServeD eclipse JGit wrapper
layout: default
---

## General information
This library is placed inside `com.verknowsys.served.utils.git` package and includes API for manipulating git repositories.
See [tests](https://github.com/teamon/svd/tree/master/svd.utils/src/test/scala/com/verknowsys/served/utils/git) for more examples

### Creating git repository
Create new normal repository (`git init`)
{% highlight scala %}
val repo = Git.init("/path/to/repo")
{% endhighlight %}

Create new bare repository (`git init --bare`)
{% highlight scala %}
val repo = Git.init("/path/to/bare/repo.git", bare = true)
{% endhighlight %}

Clone repository (`git clone`)
{% highlight scala %}
val repo = Git.clone("/path/to/repo", "remote_repo_uri.git")
{% endhighlight %}

### Getting information about repository
Basic information
{% highlight scala %}
val repo = new GitRepository("/path/to/repo")
repo.name     // name of repository (the last part of path)
repo.path     // full path
repo.isBare   // check if repository was created with `--bare` option
{% endhighlight %}

`HEAD` reference
{% highlight scala %}
repo.head
{% endhighlight %}

### Making changes
Commit file (`git add`, `git commit -m`)
{% highlight scala %}
// create a file with name README
repo.add("README")  // add file to index
repo.commit("Added README")
{% endhighlight %}

Provide author and committer
{% highlight scala %}
val someone = new Author("J. Author", "a@example.com")
val me = new Author("J. Committer","c@example.com")
repo.commit("Some changes", author = someone, committer = me)
{% endhighlight %}

### Branches
Current branch name
{% highlight scala %}
repo.currentBranch
{% endhighlight %}

Branch list
{% highlight scala %}
repo.branchList
{% endhighlight %}

Create new branch (`git branch`)
{% highlight scala %}
repo.branch("feature")
{% endhighlight %}

Checkout branch (`git checkout`)
{% highlight scala %}
repo.checkout("master")
{% endhighlight %}

### Commits
Full history (`git log`)
{% highlight scala %}
repo.history
repo.history.take(10) // last 10 commits
{% endhighlight %}

Partial history
{% highlight scala %}
repo.history("fe134..123413") // from provided sha to HEAD
repo.history("fe134..123413", "efda3413...4314") // commits between this two refs
{% endhighlight %}

Commit details
{% highlight scala %}
val commit = repo.history.next // first commit form history
commit.date
commit.sha
commit.message
commit.author    // Author object
commit.committer // Author object
{% endhighlight %}

Author object
{% highlight scala %}
author.name
author.email
{% endhighlight %}

### Remote
List remotes
{% highlight scala %}
repo.remotes
{% endhighlight %}

Add remote (`git remote add`)
{% highlight scala %}
repo.addRemote("origin", "path/to/remote")
{% endhighlight %}

Push changes to remote (`git push`)
{% highlight scala %}
repo.push
{% endhighlight %}

Pull changes from remote (`git pull`)
{% highlight scala %}
repo.pull
{% endhighlight %}

### Other
List repositories in directory
{% highlight scala %}
Git.list("some/directory") // List[GitRepository]
{% endhighlight %}

## GitStats - various statistics for repository
Get all commits grouped by author
{% highlight scala %}
val stats = new GitStats(repo)
stats.authors
{% endhighlight %}
