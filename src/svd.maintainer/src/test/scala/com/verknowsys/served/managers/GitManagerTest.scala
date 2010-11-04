package com.verknowsys.served.maintainer

import com.verknowsys.served._
import org.specs._


class GitManagerTest extends SpecificationWithJUnit {
    "GitManager" should {
        "correctly list repositories" in {
            val data = "teamon" :: "pass" :: "myUid" :: "myGid" :: "info" :: "/path/to/home" :: "/path/to/shell" :: Nil
            val account = new Account(data)

            account.userName must beEqual("teamon")
            account.pass must beEqual("pass")
            account.uid must beEqual("myUid")
            account.gid must beEqual("myGid")
            account.information must beEqual("info")
            account.homeDir must beEqual("/path/to/home")
            account.shell must beEqual("/path/to/shell")        
        }
    }

}
