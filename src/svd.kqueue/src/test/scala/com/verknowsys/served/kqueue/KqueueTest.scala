package com.verknowsys.served.kqueue

import org.specs._
import java.io._
import org.apache.commons.io.FileUtils
import scala.collection.mutable.ListBuffer

object Impl {
	implicit def StringToFile(s: String) = new File(s)
}

import Impl._

class KqueueTest extends SpecificationWithJUnit {
    final val DIR = "/tmp/served/kqueue_test"
    final val N = 300

    val range = (1 to N)
        
    def files(name: String) = range map { DIR + "/" + name + _ + ".txt" }

    def timeout {
        (1 to N/50).reverse foreach { i => 
            println("Timeout: " + i + " left")
            Thread.sleep(1000)
        }
    }
    
    "Kqueue" should {
        doBefore { setup }
        
        "catch " + N + " watches for one file" in {
            var n = 0
            val filename = DIR + "/oneshot"
            
            FileUtils.touch(filename)
            
            val watchers = range map { i =>
                Kqueue.watch(filename, attributes = true){
                    n += 1
                }
            }
                        
            FileUtils.touch(filename)
            timeout
            
            n must beEqual(N)
            
            watchers foreach { _.stop }
        }

        "catch " + N + " touched files" in {
            var n = 0
            val all = files("touch_me")
        
            all foreach { FileUtils.touch(_) }
            
            val watchers = all map {
                Kqueue.watch(_, attributes = true){
                    n += 1
                }
            }
                        
            all foreach { FileUtils.touch(_) }
            timeout
        
            n must beEqual(N)
            
            watchers foreach { _.stop }
        }
        
        
        "catch " + N + " edited files" in {
            var n = 0
            var k = 0
            val all = files("mod_me")
        
            all foreach { FileUtils.writeStringToFile(_, "x") }
        
            val editWatchers = all map { s =>
                Kqueue.watch(s, modified = true){
                    n += 1
                }
            }
            
            val attribWatchers = all map { s =>
                Kqueue.watch(s, attributes = true){
                    k += 1
                }
            }
        
            all foreach { FileUtils.writeStringToFile(_, "y") }
            timeout
        
            n must beEqual(N)
            k must beEqual(0)
            
            editWatchers foreach { _.stop }
            attribWatchers foreach { _.stop }
        }
        
        "catch " + N + " renamed files" in {
            var n = 0
            var k = 0
            var m = 0
            val all = files("rename_me")
        
            all foreach { FileUtils.touch(_) }
        
            val moveWatchers = all map { s =>
                Kqueue.watch(s, renamed = true){
                    n += 1
                }
            }
            
            val editWatchers = all map { s =>
                Kqueue.watch(s, modified = true){
                    k += 1
                }
            }
            
            val attribWatchers = all map { s =>
                Kqueue.watch(s, attributes = true){
                    m += 1
                }
            }
        
            all foreach { s => FileUtils.moveFile(s, s + ".moved") }
            timeout
        
            n must beEqual(N)
            k must beEqual(0)
            m must beEqual(0)
            
            moveWatchers foreach { _.stop }
            editWatchers foreach { _.stop }
            attribWatchers foreach { _.stop }
        }
    }


    private def setup {
        try { FileUtils.forceDelete(DIR) } catch { case _ => }
    }
}
