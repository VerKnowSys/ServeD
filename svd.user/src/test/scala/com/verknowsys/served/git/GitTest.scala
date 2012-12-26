/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.git


import java.io.File
import com.verknowsys.served.testing._
import com.verknowsys.served.utils._
import org.eclipse.jgit.api.errors.NoHeadException
// import org.specs._


class GitTest extends DefaultTest {
    val path = randomPath
    mkdir(path)

    override def afterEach {
        rmdir(path)
    }

    "GitRepository" should "create new normal repository" in {
        val repo = GitCore.init(path / "newrepo")
        repo.path should equal (path / "newrepo")
        repo.name should equal ("newrepo")
        repo should not be ('bare)

        val file = new File(path / "newrepo")
        file should (exist)
        file should be ('directory)

        val dotgit = new File(path / "newrepo" / ".git")
        dotgit should (exist)
        dotgit should be ('directory)
    }

    it should "create new bare repository" in {
        val repo = GitCore.init(path / "newbarerepo", bare = true)
        repo.name should equal ("newbarerepo")
        repo should be ('bare)

        val file = new File(path / "newbarerepo.git")
        file should (exist)
        file should be ('directory)

        val dotgit = new File(path / "newbarerepo.git" / ".git")
        dotgit should not (exist)
    }

    it should "clone remote repository" in {
        val source = GitCore.init(path)
        writeFile(source.path / "README", "Remote repo README")
        source.add("README")
        source.commit("initial for clone")

        val target = GitCore.clone(randomPath, source.path)
        target.history.size should equal (1)
        target.history.next.message should equal ("initial for clone")
    }

    it should "list repositories in directory" in {
        GitCore.list(path) should have size (0)

        GitCore.init(path / "one")
        GitCore.init(path / "two")
        GitCore.init(path / "three")

        val list = GitCore.list(path).map(_.name)
        list should have size(3)
        list should contain ("one")
        list should contain ("two")
        list should contain ("three")
    }

    "GitRepository commands" should "have null HEAD" in {
        val repo = GitCore.init(randomPath)
        repo.head.getObjectId should be (null)
    }

    it should "add new file and commit" in {
        val repo = GitCore.init(path)
        writeFile(repo.path / "README", "Some readme text")
        repo.add("README")
        repo.commit("init")

        repo.history.size should equal (1)

        val head = repo.head
        val first = repo.history.next

        head.getObjectId should not be (null)
        first.message should equal ("init")

        head.sha should equal (first.sha)

        repo.currentBranch should equal ("master")
    }

    it should "make few commits" in {
        val repo = GitCore.init(path)
        writeFile(repo.path / "README", "Some readme text")
        repo.add("README")
        repo.commit("init")

        (1 to 10) foreach { i =>
            writeFile(repo.path / "README", "Changed to " + i)
            repo.add("README")
            repo.commit("changed " + i)
        }

        repo.history.size should equal (11)
        repo.history.take(3).map(_.message).toList should equal (List("changed 10", "changed 9", "changed 8"))

        val sha3 = repo.history.drop(7).next.sha
        val sha5 = repo.history.drop(5).next.sha

        repo.history(sha3).size should equal (7)
        repo.history(sha5).size should equal (5)
    }

    it should "remote" in {
        val repo = GitCore.init(path)
        repo.remotes should have size (0)

        repo.addRemote("origin", "/path/to/remote.git")
        repo.remotes should have size (1)

        repo.addRemote("github", "githuburl.git")
        repo.remotes should have size (2)

        val remotes = repo.remotes.map(_.name)
        remotes should contain ("origin")
        remotes should contain ("github")

        val sameRepo = new GitRepository(repo.path)
        sameRepo.remotes should have size (2)
    }

    it should "push" in {
        val repo = GitCore.init(randomPath)
        writeFile(repo.path / "README", "Some readme text")
        repo.add("README")
        repo.commit("init")

        val remote = GitCore.init(randomPath)
        evaluating { remote.history } should produce [NoHeadException]

        repo.addRemote("origin", remote.path)
        repo.push()

        remote.history.size should equal (1)
    }

    it should "pull" in {
        val source = GitCore.init(randomPath)
        writeFile(source.path / "README", "Remote repo README")
        source.add("README")
        source.commit("initial")

        val target = GitCore.init(randomPath)
        target.addRemote("origin", source.path)
        target.pull

        target.history.size should equal (1)
    }

    it should "branch" in {
        val repo = GitCore.init(randomPath)
        writeFile(repo.path / "README", "Some readme text")
        repo.add("README")
        repo.commit("init")
        repo.branchList should have size (1)
        repo.branchList.map(_.name.split("/").last) should contain ("master")

        repo.branch("feature")
        repo.branchList should have size (2)
        val branches = repo.branchList.map(_.name.split("/").last)
        branches should contain ("master")
        branches should contain ("feature")
    }

    it should "checkout" in {
        val repo = GitCore.init(randomPath)
        writeFile(repo.path / "README", "Some readme text")
        repo.add("README")
        repo.commit("init")

        repo.currentBranch should equal ("master")

        repo.branch("feature")
        repo.checkout("feature")

        writeFile(repo.path / "README", "Some readme text added only in feature branch")
        repo.add("README")
        repo.commit("changed README")

        repo.history.size should equal (2)

        repo.checkout("master")
        repo.history.size should equal (1)
    }

    it should "single commit" in {
        val repo = GitCore.init(randomPath)
        writeFile(repo.path / "README", "Some readme text")
        repo.add("README")

        val me = new Author("T. Tobolski", "teamon@example.com")
        val him = new Author("D. Dettlaff", "dmilith@example.com")
        repo.commit("init", author = me, committer = him)

        val commit = repo.history.next
        commit.message should equal ("init")
        commit.author.name should equal ("T. Tobolski")
        commit.author.email should equal ("teamon@example.com")
        commit.committer.name should equal ("D. Dettlaff")
        commit.committer.email should equal ("dmilith@example.com")

        commit.sha should equal (repo.head.sha)
    }

    "Author" should "init" in {
        val author = new Author("T. Tobolski", "teamon@example.com")
        author.name should equal ("T. Tobolski")
        author.email should equal ("teamon@example.com")
    }
}
