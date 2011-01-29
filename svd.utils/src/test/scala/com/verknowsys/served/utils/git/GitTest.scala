package com.verknowsys.served.utils.git

import java.io.File
import com.verknowsys.served.spechelpers._
import com.verknowsys.served.SvdSpecHelpers._
import org.specs._

class GitTest extends Specification {
    final val DIR = "/tmp/served/gittest"

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
        }
        
        "add new file and commit" in {
            writeFile(repoRoot + "/README", "Some readme text")
            repo.add("README")
            repo.commit("init")

            repo.history must haveSize(1)
            
            val head = repo.head
            val first = repo.history.next

            head.getObjectId mustNot beNull
            first.message must beEqual("init")

            head.sha must beEqual(first.sha)
        }
        
    }
}
