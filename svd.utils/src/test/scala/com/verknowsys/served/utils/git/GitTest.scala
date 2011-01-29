package com.verknowsys.served.utils.git

import java.io.File
import com.verknowsys.served.spechelpers._
import com.verknowsys.served.SvdSpecHelpers._
import org.eclipse.jgit.api.errors.NoHeadException
import org.specs._

class GitTest extends Specification {
    final val DIR = "/tmp/served/gittest"
    
    var repoCount = 0
    
    def newRepoPath = {
        repoCount += 1
        DIR + "/repo_" + repoCount + "_" + System.currentTimeMillis
    }

    "GitRepository object" should {
        doBefore {
            rmdir(DIR)
            mkdir(DIR)
        }

        "create new normal repository" in {
            val repo = GitRepository.init(DIR+"/newrepo")
            repo.name must_== "newrepo"
            repo.isBare must beFalse
            
            val file = new File(DIR+"/newrepo")
            file.exists must beTrue
            file.isDirectory must beTrue
            
            val dotgit = new File(DIR+"/newrepo/.git")
            dotgit.exists must beTrue
            dotgit.isDirectory must beTrue
        }
        
        "create new bare repository" in {
            val repo = GitRepository.init(DIR+"/newbarerepo", bare = true)
            repo.name must_== "newbarerepo"
            repo.isBare must beTrue
            
            val file = new File(DIR+"/newbarerepo")
            file.exists must beTrue
            file.isDirectory must beTrue
            
            val dotgit = new File(DIR+"/newbarerepo/.git")
            dotgit.exists must beFalse
        }
        
        // "clone remote repository" in {
        //     prepareRemoteRepo(DIR+"/remoterepo.git")
        //     
        //     val repo = GitRepository.clone(DIR+"/cloned_from_remote", DIR+"/remoterepo.git")
        //     repo.history must haveSize(6)
        // }
    }
    
    "GitRepository commands" should {
        var repo: GitRepository = null
        val repoRoot = DIR+"/testrepo"
        
        doBefore {
            rmdir(DIR)
            mkdir(DIR)
            repo = GitRepository.init(repoRoot)
        }
        
        "have null HEAD" in {
            repo.head.getObjectId must beNull
            println(repo.currentBranch)
        }
        
        "add new file and commit" in {
            writeFile(repoRoot + "/README", "Some readme text")
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
            writeFile(repoRoot + "/README", "Some readme text")
            repo.add("README")
            repo.commit("init")

            (1 to 10) foreach { i=>
                writeFile(repoRoot + "/README", "Changed to " + i)
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
            repo.remotes must haveSize(0)

            repo.addRemote("origin", "/path/to/remote.git")
            repo.remotes must haveSize(1)

            repo.addRemote("github", "githuburl.git")
            repo.remotes must haveSize(2)

            repo.remotes.map(_.name) must containAll("origin" :: "github" :: Nil)
            
            val sameRepo = new GitRepository(repoRoot)
            sameRepo.remotes must haveSize(2)
        }
        
        "push" in {
            writeFile(repoRoot + "/README", "Some readme text")
            repo.add("README")
            repo.commit("init")
            
            prepareEmptyRemoteRepo(DIR + "/remote_repo.git")
            val remote = new GitRepository(DIR + "/remote_repo.git")
            remote.history must throwA[NoHeadException]
            
            repo.addRemote("origin", DIR + "/remote_repo.git")
            repo.push()
            
            remote.history must haveSize(1)
        }
        
        "pull" in {
            val source = GitRepository.init(newRepoPath)
            writeFile(source.gitRepo.getWorkTree.getPath + "/README", "Remote repo README")
            source.add("README")
            source.commit("initial")
            
            
            val target = GitRepository.init(newRepoPath)
            target.addRemote("origin", source.gitRepo.getWorkTree.getPath)
            target.pull
            
            
            target.history must haveSize(1)
            
        }

    }
    
    def prepareEmptyRemoteRepo(path: String) {
        val repo = GitRepository.init(path)
    }
    

    // def prepareNon
        // writeFile(path + "/README", "Remote repository README")
        // repo.add("README")
        // repo.commit("init")
        // 
        // (1 to 5) foreach { i=>
        //     writeFile(path + "/file" + i, "File " + i)
        //     repo.add(path + "/file" + i)
        //     repo.commit("added " + i)
        // }
    // }
}
