// !!!!!!!!!
//
// Test left commented to be translated to filevents!
//
// !!!!!!!!!


// package com.verknowsys.served.utils.kqueue
// 
// import com.verknowsys.served.SpecHelpers._
// import com.verknowsys.served.utils.monitor.Monitor
// import org.specs._
// import java.io._
// import org.apache.commons.io.FileUtils
// import scala.collection.mutable.ListBuffer
// import scala.actors.Actor
// 
// class KqueueTest extends Specification {
//     final val DIR = "/tmp/served/kqueue_test"
//     final val N = 50
// 
//     val range = (1 to N)
//         
//     def files(name: String) = range map { DIR + "/" + name + _ + ".txt" }
// 
//     def timeout {
//         // (1 to N/50).reverse foreach { i => 
//             // println("Timeout: " + i + " left")
//             Thread.sleep(150)
//         // }
//     }
//     
//     "Kqueue" should {
//         doBefore { setup }
//         
//         "catch " + N + " watches for one file" in {
//             var cnt = new Counter
//             val filename = DIR + "/oneshot"
//             
//             FileUtils.touch(filename)
//             
//             val watchers = range map { i =>
//                 Kqueue.watch(filename, attributes = true){
//                     cnt ! i
//                 }
//             }
//                                     
//             touch(filename)
//             timeout
//             waitWhileRunning(watchers, cnt)
//                         
//             cnt.data must containAll(range)
//             
//             watchers foreach { _.stop }
//             cnt.stop
//             waitForDeath(watchers, cnt)  
//         }
// 
//         "catch " + N + " touched files" in {
//             var cnt = new Counter
//             val all = files("touch_me")
//         
//             all foreach { touch(_) }
//             
//             val watchers = all map { name =>
//                 Kqueue.watch(name, attributes = true){
//                     cnt ! name
//                 }
//             }
//                         
//             all foreach { touch(_) }
//             timeout
//             waitWhileRunning(watchers, cnt)
//         
//             cnt.data must containAll(all)
//         
//             watchers foreach { _.stop }
//             cnt.stop
//             waitForDeath(watchers, cnt)
//         }
//         
//         
//         "catch " + N + " edited files" in {
//             var n = new Counter
//             var k = new Counter
//             
//             val all = files("mod_me")
//         
//             all foreach { FileUtils.writeStringToFile(_, "x") }
//         
//             val editWatchers = all map { s =>
//                 Kqueue.watch(s, modified = true){
//                     n ! s
//                 }
//             }
//             
//             val attribWatchers = all map { s =>
//                 Kqueue.watch(s, attributes = true){
//                     k ! s
//                 }
//             }
//         
//             all foreach { FileUtils.writeStringToFile(_, "y") }
//             timeout
//             waitWhileRunning(editWatchers, attribWatchers, n, k)
//         
//             n.data must containAll(all)
//             k.data must haveSize(0)
//             
//             (editWatchers ++ attribWatchers) foreach { _.stop }
//             n.stop
//             k.stop
//             waitForDeath(editWatchers, attribWatchers, n, k)
//         }
//         
//         "catch " + N + " renamed files" in {
//             var n = new Counter
//             var k = new Counter
//             var m = new Counter
//             val all = files("rename_me")
//         
//             all foreach { FileUtils.touch(_) }
//         
//             val moveWatchers = all map { s =>
//                 Kqueue.watch(s, renamed = true){
//                     n ! s
//                 }
//             }
//             
//             val editWatchers = all map { s =>
//                 Kqueue.watch(s, modified = true){
//                     k ! s
//                 }
//             }
//             
//             val attribWatchers = all map { s =>
//                 Kqueue.watch(s, attributes = true){
//                     m ! s
//                 }
//             }
//         
//             all foreach { s => FileUtils.moveFile(s, s + ".moved") }
//             timeout
//             waitWhileRunning(moveWatchers, editWatchers, attribWatchers, n, k, m)
//         
//             n.data must containAll(all)
//             k.data must haveSize(0)
//             m.data must haveSize(0)
//             
//             (moveWatchers ++ editWatchers ++ attribWatchers) foreach { _.stop }
//             n.stop
//             k.stop
//             m.stop
//             waitForDeath(moveWatchers, editWatchers, attribWatchers, n, k, m)
//         }
//     }
// 
// 
//     private def setup {
//         Monitor.start
//         // waitForEnter
//         
//         try { FileUtils.forceDelete(DIR) } catch { case _ => }
//     }
// }
