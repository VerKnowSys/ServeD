// package com.verknowsys.served.maintainer
// 
// import com.verknowsys.served.utils.signals._
// import com.verknowsys.served.SvdSpecHelpers._
// import com.verknowsys.served.SvdConfig
// import org.specs._
// import scala.actors.Actor
// 
// import com.verknowsys.served.utils.monitor.SvdMonitor
// 
// 
// class SvdAccountsManagerTest extends Specification {
//     "SvdAccountsManager" should {
//         "create manager for each account" in {
//             SvdMonitor.start
//             
//             def changePasswdPath(path: String) {
//                 val passwd = readFile(System.getProperty("user.dir") + "/svd.maintainer/src/test/resources/etc/" + path)
//                 writeFile(SvdConfig.systemPasswdFile, passwd)
//             }      
//             
//             def waitForSvdKqueue = waitFor(500) 
//             
//             SvdAccountsManager ! Init
//             
//             // No accounts
//             changePasswdPath("emptyPasswd")
//             waitForSvdKqueue
//             waitWhileRunning(SvdAccountsManager)
//             
//             SvdAccountsManager.managers must beEmpty
//             
//             // One account
//             changePasswdPath("standardPasswd")
//             waitForSvdKqueue
//             waitWhileRunning(SvdAccountsManager)
//             
//             val res1 = SvdAccountsManager.managers
//             res1 must haveSize(1)
//             val account = res1(0).account 
//             account must beEqual(SvdAccount("teamon", "*", "1001", "1001", "User &", "/home/teamon", "/usr/local/bin/zsh"))
//             account.userName must beEqual("teamon")
//             account.pass must beEqual("*")
//             account.uid must beEqual("1001")
//             account.gid must beEqual("1001")
//             account.information must beEqual("User &")
//             account.homeDir must beEqual("/home/teamon")
//             account.shell must beEqual("/usr/local/bin/zsh")
//             
//             changePasswdPath("fivePasswd")
//             waitForSvdKqueue
//             waitWhileRunning(SvdAccountsManager)
//             val res2 = SvdAccountsManager.managers
//             val users = res2.map(_.account.userName)
//             users must haveSize(5)
//             users must containAll("teamon" :: "dmilith" :: "foo" :: "bar" :: "baz" :: Nil)
//             
// 
//             SvdAccountsManager ! Quit
//             waitForDeath(SvdAccountsManager)
//         }
//     }
//     
// }