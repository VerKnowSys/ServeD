package com.verknowsys.served.git

import com.verknowsys.served.SvdSpecHelpers._
import com.verknowsys.served.utils._
import com.verknowsys.served._

import java.io.File
import org.specs._


class GitStatsTest extends Specification {
    final val DIR = SvdConfig.systemTmpDir / "served/gittest"
    

    "GitRepository stats commands" should {
        doBefore {
            rmdir(DIR)
            mkdir(DIR)
        }
        
        "make few commits" in {
            val repo = Git.init(DIR / "repo_for_stats")
            val me = new Author("T. Tobolski", "teamon@example.com")
            val him = new Author("D. Dettlaff", "dmilith@example.com")

            (1 to 3) foreach { i=>
                writeFile(repo.path / "README", "Changed to " + i)
                repo.add("README")
                repo.commit("changed " + i, author = me)
            }
            
            (4 to 10) foreach { i=>
                writeFile(repo.path / "README", "Changed to " + i)
                repo.add("README")
                repo.commit("changed " + i, author = him)
            }
            
            val stats = new GitStats(repo)
            stats.authors.mapValues(_.size) must_== Map("T. Tobolski" -> 3, "D. Dettlaff" -> 7)
        }
        
    }
}
