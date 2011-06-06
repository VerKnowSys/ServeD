package com.verknowsys.served.utils.git

import java.io.File
import com.verknowsys.served.spechelpers._
import com.verknowsys.served.SvdSpecHelpers._
import org.specs._

class GitStatsTest extends Specification {
    final val DIR = "/tmp/served/gittest"
    

    "GitRepository commands" should {
        doBefore {
            rmdir(DIR)
            mkdir(DIR)
        }
        
        "make few commits" in {
            val repo = Git.init(DIR + "/repo_for_stats")
            val me = new Author("T. Tobolski", "teamon@example.com")
            val him = new Author("D. Dettlaff", "dmilith@example.com")

            (1 to 3) foreach { i=>
                writeFile(repo.path + "/README", "Changed to " + i)
                repo.add("README")
                repo.commit("changed " + i, author = me)
            }
            
            (4 to 10) foreach { i=>
                writeFile(repo.path + "/README", "Changed to " + i)
                repo.add("README")
                repo.commit("changed " + i, author = him)
            }
            
            val stats = new GitStats(repo)
            stats.authors.mapValues(_.size) must_== Map("T. Tobolski" -> 3, "D. Dettlaff" -> 7)
        }
        
    }
}
