package com.verknowsys.served.utils.git

import java.io.File
import com.verknowsys.served.spechelpers._
import com.verknowsys.served.SvdSpecHelpers._
import org.specs._

class GitTest extends Specification {
    final val DIR = "/tmp/served/gittest"
    
    "GitRepository" should {
        doBefore {
            rmdir(DIR)
            mkdir(DIR)
        }
        
        "create new normal repository" in {
            val repo = GitRepository.init(DIR+"/newrepo")
            repo.name must_== "newrepo"
            
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
            
            val file = new File(DIR+"/newbarerepo")
            file.exists must beTrue
            file.isDirectory must beTrue
            
            val dotgit = new File(DIR+"/newbarerepo/.git")
            dotgit.exists must beFalse
            
        }
    }
}
