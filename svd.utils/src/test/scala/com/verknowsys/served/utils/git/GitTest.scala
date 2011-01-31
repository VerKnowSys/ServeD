package com.verknowsys.served.utils.git

import java.io.File
import com.verknowsys.served.spechelpers._
import com.verknowsys.served.SvdSpecHelpers._
import com.verknowsys.served.utils._
import org.eclipse.jgit.api.errors.NoHeadException
import org.specs._

class GitTest extends Specification {

    "GitRepository object" should {
        doBefore {
            reloadTestDir
        }

        "create new normal repository" in {
            val repo = Git.init(testPath("newrepo"))
            repo.path must_== testPath("newrepo")
            repo.name must_== "newrepo"
            repo.isBare must beFalse
            
            val file = new File(testPath("newrepo"))
            file.exists must beTrue
            file.isDirectory must beTrue
            
            val dotgit = new File(testPath("newrepo/.git"))
            dotgit.exists must beTrue
            dotgit.isDirectory must beTrue
        }
        
        "create new bare repository" in {
            val repo = Git.init(testPath("newbarerepo"), bare = true)
            repo.name must_== "newbarerepo"
            repo.isBare must beTrue
            
            val file = new File(testPath("newbarerepo.git"))
            file.exists must beTrue
            file.isDirectory must beTrue
            
            val dotgit = new File(testPath("newbarerepo.git/.git"))
            dotgit.exists must beFalse
        }
        
        "clone remote repository" in {
            val source = Git.init(randomPath)
            writeFile(source.path / "README", "Remote repo README")
            source.add("README")
            source.commit("initial for clone")
            
            val target = Git.clone(randomPath, source.path)
            target.history must haveSize(1)
            target.history.next.message must_== "initial for clone"
        }
        
        "list repositories in directory" in {
            val path = randomPath
            Git.list(path) must haveSize(0)
            
            Git.init(path / "one")
            Git.init(path / "two")
            Git.init(path / "three")
            
            val list = Git.list(path)
            list must haveSize(3)
            list.map(_.name) must containAll("one" :: "two" :: "three" :: Nil)
            
        }
    }
    
    "GitRepository commands" should {
        doBefore {
            reloadTestDir
        }
        
        "have null HEAD" in {
            val repo = Git.init(randomPath)
            repo.head.getObjectId must beNull
        }
        
        "add new file and commit" in {
            val repo = Git.init(randomPath)
            writeFile(repo.path / "README", "Some readme text")
            repo.add("README")
            repo.commit("init")

            repo.history must haveSize(1)
            
            val head = repo.head
            val first = repo.history.next

            head.getObjectId mustNot beNull
            first.message must_== "init"

            head.sha must_== first.sha
            
            repo.currentBranch must_== "master"
        }
        
        "make few commits" in {
            val repo = Git.init(randomPath)
            writeFile(repo.path / "README", "Some readme text")
            repo.add("README")
            repo.commit("init")

            (1 to 10) foreach { i =>
                writeFile(repo.path / "README", "Changed to " + i)
                repo.add("README")
                repo.commit("changed " + i)
            }


            repo.history must haveSize(11)
            
            repo.history.take(3).map(_.message).toList must_== List("changed 10", "changed 9", "changed 8")
            
            val sha3 = repo.history.drop(7).next.sha
            val sha5 = repo.history.drop(5).next.sha
            
            repo.history(sha3) must haveSize(7)
            repo.history(sha5) must haveSize(5)
        }
        
        "remote" in {
            val repo = Git.init(randomPath)
            repo.remotes must haveSize(0)

            repo.addRemote("origin", "/path/to/remote.git")
            repo.remotes must haveSize(1)

            repo.addRemote("github", "githuburl.git")
            repo.remotes must haveSize(2)

            repo.remotes.map(_.name) must containAll("origin" :: "github" :: Nil)
            
            val sameRepo = new GitRepository(repo.path)
            sameRepo.remotes must haveSize(2)
        }
        
        "push" in {
            val repo = Git.init(randomPath)
            writeFile(repo.path / "README", "Some readme text")
            repo.add("README")
            repo.commit("init")
            
            val remote = Git.init(randomPath)
            remote.history must throwA[NoHeadException]
            
            repo.addRemote("origin", remote.path)
            repo.push()
            
            remote.history must haveSize(1)
        }
        
        "pull" in {
            val source = Git.init(randomPath)
            writeFile(source.path / "README", "Remote repo README")
            source.add("README")
            source.commit("initial")


            val target = Git.init(randomPath)
            target.addRemote("origin", source.path)
            target.pull

            target.history must haveSize(1)
        }
        
        "branch" in {
            val repo = Git.init(randomPath)
            writeFile(repo.path / "README", "Some readme text")
            repo.add("README")
            repo.commit("init")
            
            repo.branchList must haveSize(1)
                        
            repo.branchList.map(_.name.split("/").last) must contain("master")
            
            repo.branch("feature")
            
            repo.branchList must haveSize(2)
            repo.branchList.map(_.name.split("/").last) must containAll("master" :: "feature" :: Nil)
        }
        
        "checkout" in {
            val repo = Git.init(randomPath)
            writeFile(repo.path / "README", "Some readme text")
            repo.add("README")
            repo.commit("init")
            
            repo.currentBranch must_== "master"
            
            repo.branch("feature")
            repo.checkout("feature")
            
            writeFile(repo.path / "README", "Some readme text added only in feature branch")
            repo.add("README")
            repo.commit("changed README")
            
            repo.history must haveSize(2)
            
            repo.checkout("master")
            repo.history must haveSize(1)
        }
        
        "single commit" in {
            val repo = Git.init(randomPath)
            writeFile(repo.path / "README", "Some readme text")
            repo.add("README")
            
            val me = new Author("T. Tobolski", "teamon@example.com")
            val him = new Author("D. Dettlaff", "dmilith@example.com")
            repo.commit("init", author = me, committer = him)
            
            val commit = repo.history.next
            commit.message must_== "init"
            commit.author.name must_== "T. Tobolski"
            commit.author.email must_== "teamon@example.com"
            commit.committer.name must_== "D. Dettlaff"
            commit.committer.email must_== "dmilith@example.com"
            
            commit.sha must_== repo.head.sha
        }

    }
    
    "Author" should {
        "init" in {
            val author = new Author("T. Tobolski", "teamon@example.com")
            author.name must_== "T. Tobolski"
            author.email must_== "teamon@example.com"
        }
    }

}
