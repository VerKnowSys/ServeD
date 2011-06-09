// 2011-06-09 19:31:18 - dmilith - PENDING: XXX: FIXME: TODO: FIx spec for reply_? with Success
// package com.verknowsys.served.systemmanager
// 
// 
// import com.verknowsys.served.systemmanager.acl._
// import com.verknowsys.served.systemmanager.native._
// import com.verknowsys.served.systemmanager.managers._
// import com.verknowsys.served.utils.SvdFileEventsManager
// import com.verknowsys.served.SvdSpecHelpers._
// import com.verknowsys.served.SvdConfig
// import com.verknowsys.served.api._
// import org.specs._
// import akka.testkit.TestKit
// 
// import akka.actor._
// import akka.actor.Actor._
// 
// 
// class SvdAccountsManagerTest extends Specification with TestKit {
//     
//     
//     def changePasswdPath(path: String) {
//         val passwd = readFile(System.getProperty("user.dir") + "/svd.core/src/test/resources/etc/" + path)
//         writeFile(SvdConfig.systemPasswdFile, passwd)
//     }      
//     
//     
//     def waitForKqueue = waitFor(SvdConfig.kqueueWaitInterval)
//     
//     
//     var am: ActorRef = null
//     var fem: ActorRef = null
//     
//     "SvdAccountsManager" should {
//         doBefore {
//             fem = actorOf[SvdFileEventsManager].start
//         }
//         
//         doAfter { 
//             registry.shutdownAll 
//         }
//         
//         "not spawn any Account Managers" in {
//             changePasswdPath("emptyPasswd")
//             
//             registry.actorsFor[SvdAccountManager] must haveSize(0)
//             am = actorOf[SvdAccountsManager].start
//             am !! Init
//             
//             expectMsg(Success)
//             
//             registry.actorsFor[SvdAccountManager] must haveSize(0)
//         }
//         
//         "spawn one Account Manager" in {
//             changePasswdPath("standardPasswd")
//             
//             registry.actorsFor[SvdAccountManager] must haveSize(0)
//             am = actorOf[SvdAccountsManager].start
//             am !! Init
//             expectMsg(Success)
//             
//             val managers = registry.actorsFor[SvdAccountManager]
//             managers must haveSize(1)
//             managers.map(a => (a !! GetAccount).get) must
//                 contain(SvdAccount("teamon", "*", 1001, 1001, "User &", "/home/teamon", "/usr/local/bin/zsh", Nil))
//         }
//         
//         "spawn few Account Managers" in {
//             changePasswdPath("fivePasswd")
//             
//             registry.actorsFor[SvdAccountManager] must haveSize(0)
//             am = actorOf[SvdAccountsManager].start
//             am !! Init
//             expectMsg(Success)
//             
//             val managers = registry.actorsFor[SvdAccountManager]
//             managers must haveSize(5)
//             val accounts = managers.map(a => (a !! GetAccount)).collect {
//                 case Some(a: SvdAccount) => a
//             }
//             
//             accounts.map(_.userName) must containAll("teamon" :: "dmilith" :: "foo" :: "bar" :: "baz" :: Nil)
//         }
//      }
//      
//      
// }
