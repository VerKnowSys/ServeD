package com.verknowsys.served.kqueue

import org.specs._
import java.io._
import org.apache.commons.io.FileUtils
import scala.collection.mutable.ListBuffer

object Impl {
	implicit def StringToFile(s: String) = new File(s)
}

import Impl._

class FileEventsTest extends SpecificationWithJUnit {
    final val DIR = "/tmp/served/file_events_test"
	final val num = 10

    "FileEvents" should {
        doBefore { setup }

        "notify ..." in {
			var cnt = new ListBuffer[Int]
			
			1 to num foreach { i =>
				FileEvents.watch(DIR + "/m_file" + i + ".txt", modify = true){
					cnt += i
				}
			}

			1 to num foreach { i =>
				FileUtils.writeStringToFile(DIR + "/m_file" + i + ".txt", "xxx")
			}
			
			println(cnt.toList.sortWith(_<_))

			cnt.length must beEqual(100)
        }
    }


    private def setup {
        try { FileUtils.forceDelete(DIR) } catch { case _ => }
		1 to num foreach { i =>
			FileUtils.writeStringToFile(DIR + "/m_file" + i + ".txt", "x")
		}
    }
}
