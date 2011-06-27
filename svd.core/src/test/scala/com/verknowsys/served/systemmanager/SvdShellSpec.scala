// package com.verknowsys.served.systemmanager.native
// 
// 
// import com.verknowsys.served.SvdSpecHelpers._
// import com.verknowsys.served.systemmanager._
// import com.verknowsys.served.utils.signals._
// import com.verknowsys.served.utils._
// import com.verknowsys.served._
// // import SvdPOSIX._
// import com.verknowsys.served.systemmanager.native._
// 
// import org.apache.commons.io.FileUtils
// import org.hyperic.sigar._
// import org.specs._
// import java.lang._
// import java.io._
// 
// 
// class SvdShellTest extends Specification {
// 
//     
//     "SvdShellTest" should {
//         
//         "empty command should be accepted. restart should work properly" in {
//             var sh = new SvdShell(new SvdAccount(userName = System.getProperty("user.name"), homeDir = randomPath))
//             sh.spawn("")
//             sh.dead must_== false
//             sh.close
//             sh.dead must_== true
//             sh.spawn("") must throwAn[Exception]
//             sh.restart
//             sh.spawn("")
//             sh.dead must_== false
//             sh.output.head must_== ""
//             sh.output.tail.head must_== ""
//         }
//         
//         // "invalid command should be handled properly" in {
//         //     var sh = new SvdShell(new SvdAccount(userName = System.getProperty("user.name"), homeDir = randomPath))
//         //     sh.spawn("mkdir /sdgerg/dsfgfd", "No such file or directory")
//         //     sh.dead must_== false
//         //     sh.restart
//         //     sh.dead must_== true
//         // }
//         
//     }
// 
//         // "example dig command should pass" ! {
//         //                 val sh = new SvdShell(new SvdAccount(userName = System.getProperty("user.name"), homeDir = randomPath))
//         //                 sh.spawn("mkdir /sdgerg/dsfgfd")
//         //                 sh.shell.expect("No such file or directory")
//         //                 sh.close
//         //                 sh.dead must beEqual(true)
//         // }^
// 
// 
// //    try {
// //             val sh = new SvdShell(new SvdAccount(userName = System.getProperty("user.name"), homeDir = randomPath))
// //             sh.spawn("dig +trace wp.pl")
// //             sh.shell.expect(";; Received")
// //             sh.shell.expect("wp.pl")
// //             sh.close
// //             sh.dead must beEqual(true)
// //         } catch {
// //             case e: Exception => 
// //                 fail("Shouldn't throw exception for dig")
// //         }
// // 
// //         try {
// //             val sh = new SvdShell(new SvdAccount(userName = System.getProperty("user.name"), homeDir = randomPath))
// //             sh.spawn("mkdir /sdgerg/dsfgfd")
// //             sh.shell.expect("No such file or directory")
// //             sh.close
// //             sh.dead must beEqual(true)
// //         } catch {
// //             case e: Exception => 
// //                  failure
// //                 fail("Shouldn't throw exception for mkdir: " + e.getMessage)
// //         }
// //         
// //         val sh2 = new SvdShell(new SvdAccount(userName = System.getProperty("user.name"), homeDir = randomPath))
// //         sh2.dead must beEqual(false)
// //         sh2.close
// //         sh2.dead must beEqual(true)
// //     }
// // }
// }
