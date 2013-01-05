// package com.verknowsys.served.utils.kqueue


// import java.io._
// import org.apache.commons.io.FileUtils
// import scala.collection.mutable.ListBuffer
// import akka.testkit.TestActorRef
// import com.typesafe.config.ConfigFactory
// import scala.concurrent._
// import akka.pattern.ask
// import akka.remote._
// import scala.concurrent.Duration
// import akka.util.Timeout
// import akka.testkit.TestKit
// import scala.concurrent.duration._
// import akka.actor.ActorSystem
// import akka.actor._

// import com.verknowsys.served._
// import com.verknowsys.served.utils._
// import com.verknowsys.served.api.Logger
// import com.verknowsys.served.managers.LoggingManager
// import com.verknowsys.served.testing._


// class SvdKqueueTest extends DefaultTest {
//     final val DIR = "/tmp/served/kqueue_test"
//     final val N = 50

//     override def beforeAll {
//         setup
//     }


//     val range = (1 to N)

//     def files(name: String) = range map { DIR + "/" + name + _ + ".txt" }

//     def timeout {
//         // (1 to N/50).reverse foreach { i =>
//             // println("Timeout: " + i + " left")
//             Thread.sleep(550)
//         // }
//     }


//     it should "catch " + N + " watches for one file" in {
//         var cnt = 0
//         val filename = DIR + "/oneshot"

//         touch(filename)

//         val watchers = range map { i =>
//             SvdKqueue.watch(filename, attributes = true){
//                 cnt += i
//             }
//         }

//         touch(filename)
//         timeout
//         waitWhileRunning(watchers)

//         // cnt.data must containAll(range)

//         watchers foreach { _.stop }
//         // cnt.stop
//         waitForDeath(watchers)
//     }


//     it should "catch " + N + " touched files" in {
//         var cnt = 0
//         val all = files("touch_me")

//         all foreach { touch(_) }

//         val watchers = all map { name =>
//             SvdKqueue.watch(name, attributes = true){
//                 cnt += 1
//             }
//         }

//         all foreach { touch(_) }
//         timeout
//         waitWhileRunning(watchers)

//         // cnt.data must containAll(all)

//         watchers foreach { _.stop }
//         // cnt.stop
//         waitForDeath(watchers)
//     }


//     it should "catch " + N + " edited files" in {
//         var n = new Counter
//         var k = new Counter

//         val all = files("mod_me")

//         all foreach { FileUtils.writeStringToFile(_, "x") }

//         val editWatchers = all map { s =>
//             SvdKqueue.watch(s, modified = true){
//                 n ! s
//             }
//         }

//         val attribWatchers = all map { s =>
//             SvdKqueue.watch(s, attributes = true){
//                 k ! s
//             }
//         }

//         all foreach { FileUtils.writeStringToFile(_, "y") }
//         timeout
//         waitWhileRunning(editWatchers, attribWatchers, n, k)

//         n.data must containAll(all)
//         k.data must haveSize(0)

//         (editWatchers ++ attribWatchers) foreach { _.stop }
//         n.stop
//         k.stop
//         waitForDeath(editWatchers, attribWatchers, n, k)
//     }


//     it should "catch " + N + " renamed files" in {
//         var n = new Counter
//         var k = new Counter
//         var m = new Counter
//         val all = files("rename_me")

//         all foreach { FileUtils.touch(_) }

//         val moveWatchers = all map { s =>
//             SvdKqueue.watch(s, renamed = true){
//                 n ! s
//             }
//         }

//         val editWatchers = all map { s =>
//             SvdKqueue.watch(s, modified = true){
//                 k ! s
//             }
//         }

//         val attribWatchers = all map { s =>
//             SvdKqueue.watch(s, attributes = true){
//                 m ! s
//             }
//         }

//         all foreach { s => FileUtils.moveFile(s, s + ".moved") }
//         timeout
//         waitWhileRunning(moveWatchers, editWatchers, attribWatchers, n, k, m)

//         n.data must containAll(all)
//         k.data must haveSize(0)
//         m.data must haveSize(0)

//         (moveWatchers ++ editWatchers ++ attribWatchers) foreach { _.stop }
//         n.stop
//         k.stop
//         m.stop
//         waitForDeath(moveWatchers, editWatchers, attribWatchers, n, k, m)
//     }


//     private def setup {
//         // SvdMonitor.start
//         // waitForEnter

//         try { FileUtils.forceDelete(DIR) } catch { case _ => }
//     }
// }
