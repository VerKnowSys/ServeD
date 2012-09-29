package com.verknowsys.served.managers


import akka.testkit.TestActorRef
import com.typesafe.config.ConfigFactory
import akka.dispatch._
import akka.pattern.ask
import akka.remote._
import akka.util.Duration
import akka.util.Timeout
import akka.testkit.TestKit
import akka.util.duration._
import akka.actor.{ActorSystem, Props}

import com.verknowsys.served._
import com.verknowsys.served.utils._
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.utils.SvdFileEventsManager
import com.verknowsys.served.SvdConfig
import com.verknowsys.served.api._
import com.verknowsys.served.testing._


class SvdAccountManagerTest(_system: ActorSystem) extends TestKit(_system) with DefaultTest {

    def this() = this(ActorSystem("svd-test-system"))


    def changePasswdPath(path: String) {
        val passwd = readFile(System.getProperty("user.dir") + "/svd.core/src/test/resources/etc/" + path).toString
        writeFile(SvdConfig.temporaryDir + "/test-124rwefsd", passwd)
    }


    def waitForKqueue = waitFor(SvdConfig.kqueueWaitInterval)

    import CLibrary._
    val clib = CLibrary.instance
    val account = currentAccount.copy(uid = clib.getuid)
    val am = system.actorOf(Props(new SvdAccountManager(account)))
    // val am = system.actorOf(Props(new SvdAccountManager(SvdAccount(uid = 501)))) // XXX: hardcode
    var fem: ActorRef = null


    override def beforeAll {
        fem = system.actorOf(Props(new SvdFileEventsManager))
    }


    override def afterAll {
        system.shutdown
    }


    // it should "not spawn any Account Managers" in {
    //     changePasswdPath("emptyPasswd")

    //     (am ? Init) onSuccess {
    //         case Success =>
    //             true must be(true)
    //     }
    //     // system.system.actorFor(Props(new SvdAccountManager)) must haveSize(0)
    // }


    // it should "spawn one Account Manager" in {
        // changePasswdPath("standardPasswd")

        // system.system.actorFor("/user/SvdAccountManager") must haveSize(0)
        // (am ? Init) onSuccess {
        //     case Success =>
        //         true must be(true)
        // }

        // val managers = system.actorFor("/user/SvdAccountManager")
        // managers must haveSize(1)
        // managers.map(a => (a !! GetAccount).get) must
        //     contain(SvdAccount("teamon", "*", 1001, 1001, "User &", "/home/teamon", "/usr/local/bin/zsh", Nil))
    // }


    // it should "spawn few Account Managers" in {
        // changePasswdPath("fivePasswd")

        // system.system.actorFor("/user/SvdAccountManager") must haveSize(0)
        // (am ? Init) onSuccess {
        //     case Success =>
        //         true must be(true)
        // }
        // val managers = registry.actorsFor[SvdAccountManager]
        // managers must haveSize(5)
        // val accounts = managers.map(a => (a !! GetAccount)).collect {
        //     case Some(a: SvdAccount) => a
        // }

        // accounts.map(_.userName) must containAll("teamon" :: "dmilith" :: "foo" :: "bar" :: "baz" :: Nil)
    // }


    it should "not respond to GetAccount before Init" in {
        (am ? GetAccount(account.uid)) onSuccess {
            case res: SvdAccount =>
                fail("Shouldn't have available started context")

            case Error(message) =>
                message must be("Unknown SvdAccountManager message: GetAccount(501)")

        }
    }


    it should "respond to GetAccount" in {
        (am ? Init) onSuccess { // after init it should become started, after which GetAccount message might be served:
            case _ =>
                (am ? GetAccount(account.uid)) onSuccess {
                    case res: SvdAccount =>
                        true must be(true)
                    case _ =>
                        fail("No test account?")
                }
        }
    }


}
