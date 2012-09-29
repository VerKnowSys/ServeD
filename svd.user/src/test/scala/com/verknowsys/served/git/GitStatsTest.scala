package com.verknowsys.served.git


import com.verknowsys.served.testing._
import com.verknowsys.served.utils._


class GitStatsTest extends DefaultTest {
    val path = randomPath
    mkdir(path)

    override def afterEach {
        rmdir(path)
    }

    it should "make few commits" in {
        val repo = Git.init(path / "repo_for_stats")
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
        stats.authors.mapValues(_.size) should equal (Map("T. Tobolski" -> 3, "D. Dettlaff" -> 7))
    }
}
