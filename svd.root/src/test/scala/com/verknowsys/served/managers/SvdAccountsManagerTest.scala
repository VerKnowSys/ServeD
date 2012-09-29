//2011-06-09 19:31:18 - dmilith - PENDING: XXX: FIXME: TODO: FIx spec for reply_? with Success
package com.verknowsys.served.managers


// import com.verknowsys.served.systemmanager.acl._
import com.verknowsys.served._
import com.verknowsys.served.utils._
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.systemmanager.managers._
import com.verknowsys.served.utils.SvdFileEventsManager
import com.verknowsys.served.SvdConfig
import com.verknowsys.served.api._
import org.specs._

import akka.testkit.TestActorRef
import com.typesafe.config.ConfigFactory
import akka.dispatch._
import akka.pattern.ask
import akka.remote._
import akka.util.Duration
import akka.util.Timeout
import akka.testkit.TestKit
import akka.util.duration._
import akka.actor.ActorSystem

import com.verknowsys.served.testing._
import akka.actor.Props


class SvdAccountsManagerTest(_system: ActorSystem) extends TestKit(_system) with DefaultTest {

    def this() = this(ActorSystem("svd-test-system"))


    def changePasswdPath(path: String) {
        val passwd = readFile(System.getProperty("user.dir") + "/svd.core/src/test/resources/etc/" + path).toString
        writeFile(SvdConfig.temporaryDir + "/test-124rwefsd", passwd)
    }


    def waitForKqueue = waitFor(SvdConfig.kqueueWaitInterval)


    var am: ActorRef = null
    var fem: ActorRef = null

    override def beforeAll {
        fem = system.actorOf(Props(new SvdFileEventsManager))
    }

    override def afterAll {
        system.shutdown
    }



    it should "not spawn any Account Managers" in {
        changePasswdPath("emptyPasswd")

        // system.actorFor("/user/SvdAccountManager") must haveSize(0)
        am = system.actorOf(Props(new SvdAccountsManager))
        (am ? Init) onSuccess {
            case Success =>
                true must be(true)
        }

        // system.system.actorFor(Props(new SvdAccountManager)) must haveSize(0)
    }

    it should "spawn one Account Manager" in {
        changePasswdPath("standardPasswd")

        // system.system.actorFor("/user/SvdAccountManager") must haveSize(0)
        am = system.actorOf(Props(new SvdAccountsManager))
        (am ? Init) onSuccess {
            case Success =>
                expectMsg(Success)
        }

        // val managers = system.actorFor("/user/SvdAccountManager")
        // managers must haveSize(1)
        // managers.map(a => (a !! GetAccount).get) must
        //     contain(SvdAccount("teamon", "*", 1001, 1001, "User &", "/home/teamon", "/usr/local/bin/zsh", Nil))
    }

    it should "spawn few Account Managers" in {
        changePasswdPath("fivePasswd")

        // system.system.actorFor("/user/SvdAccountManager") must haveSize(0)
        am = system.actorOf(Props(new SvdAccountsManager))
        (am ? Init) onSuccess {
            case Success =>
                expectMsg(Success)
        }

        // val managers = registry.actorsFor[SvdAccountManager]
        // managers must haveSize(5)
        // val accounts = managers.map(a => (a !! GetAccount)).collect {
        //     case Some(a: SvdAccount) => a
        // }

        // accounts.map(_.userName) must containAll("teamon" :: "dmilith" :: "foo" :: "bar" :: "baz" :: Nil)
    }
}
