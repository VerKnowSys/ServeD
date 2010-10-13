package com.verknowsys.served.maintainer

import com.verknowsys.served._
import com.verknowsys.served.maintainer._
import org.specs._
import org.apache.commons.io.FileUtils
import java.io.File


class AccountTest extends SpecificationWithJUnit {
    
    "Account" should {
        val account = Account(userName = "teamon", homeDir = "/tmp/svd/home/teamon/")
        try { FileUtils.forceDelete(new File("/tmp/svd/home/teamon/")) } catch { case _ => }
        FileUtils.forceMkdir(new File("/tmp/svd/home/teamon/"))

        "Have empty repositories list" in {
            account.repositories must beEmpty
        }
        
        "Create new git repository" in {
            account.createRepository("testrepo")
            "/tmp/svd/home/teamon/git/testrepo.git" must beAnExistingPath
            account.repositories.size must beEqual(1)
        }
    }

}
