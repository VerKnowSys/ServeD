// // © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// // This Software is a close code project. You may not redistribute this code without permission of author.
// 
// package com.verknowsys.served.utils.scalabot
// 
// 
// import com.verknowsys.served.utils.commiter.Commit
// import com.verknowsys.served.utils._
// 
// import org.junit.Ignore
// import scala._
// import junit.framework._;
// import Assert._;
// 
// 
// object SvdBotTests {
//     def suite: Test = {
//         val suite = new TestSuite(classOf[CommitTest])
//         suite.addTestSuite(classOf[SvdConfigTest])
//         suite
//     }
// 
//     def main(args: Array[String]) {
//         junit.textui.TestRunner.run(suite);
//     }
// }
// 
// 
// class CommitTest extends TestCase("commit") with SvdUtils {
//     var commit: Commit = null
// 
//     override def setUp {
//         commit = null
//     }
// 
//     def testCommitCreation = {
//         try {
//             commit = new Commit("")
//             fail
//         } catch {
//             case a: Throwable => {
//             }
//         }
//     }
// 
//     def testCommitCreation2 = {
//         commit = new Commit("Something")
//         assertTrue(commit != null)
//     }
// }
// 
// 
// class SvdConfigTest extends TestCase("config") {
//     //   var settings: Preferences = _
// 
//     override def setUp {
//         //       settings = new Preferences
//     }
// 
//     def testXML2 = {
//         //       settings.value("xmppPort") = 666
//         //       assertTrue( settings.loadPreferences != null )
//         //       println( settings.geti("xmppPort") )
//         assertTrue(true)
//     }
//     //
//     //   def testSvdConfigReadWrite = {
//     //       try {
//     ////         settings.savePreferences("testSvdConfig.xml")
//     //       } catch {
//     //           case _ => {
//     //               fail("Cannot save preferences file!")
//     //           }
//     //       }
//     //       try {
//     //           settings.loadPreferences
//     //       } catch {
//     //           case _ => {
//     //               fail("Cannot load preferences file!")
//     //           }
//     //       }
//     //   }
// 
//     //   def testSvdConfigSettings = {
//     //       assertTrue(settings.get("configFile").equals("testing.config"))
//     //       settings.value("gitRepositoryProjectDir") = "/something"
//     //       assertTrue(settings.get("gitRepositoryProjectDir").equals("/something"))
//     //   }
// 
// }
