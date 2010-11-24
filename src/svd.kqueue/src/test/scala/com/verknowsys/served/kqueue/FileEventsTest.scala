package com.verknowsys.served.kqueue

import org.specs._
import java.io._
import org.apache.commons.io.FileUtils

object Impl {
	implicit def StringToFile(s: String) = new File(s)
}

import Impl._

class FileEventsTest extends SpecificationWithJUnit {
    final val DIR = "/tmp/served/file_events_test"

    "FileEvents" should {
        doBefore { setup }

        "notify ..." in {
			var modCount1 = 0
			var modCount2 = 0
			
			FileEvents.watch(DIR + "/m_file001.txt", modify = true){
				modCount1 += 1
			}
			
			FileEvents.watch(DIR + "/m_file002.txt", modify = true){
				modCount2 += 1
			}
			
			Thread.sleep(2000)

            FileUtils.writeStringToFile(DIR + "/m_file001.txt", "a")
            FileUtils.writeStringToFile(DIR + "/m_file001.txt", "b")
            FileUtils.writeStringToFile(DIR + "/m_file001.txt", "c")

            FileUtils.writeStringToFile(DIR + "/m_file002.txt", "a")
            FileUtils.writeStringToFile(DIR + "/m_file002.txt", "b")
            FileUtils.writeStringToFile(DIR + "/m_file002.txt", "c")
            FileUtils.writeStringToFile(DIR + "/m_file002.txt", "d")
            FileUtils.writeStringToFile(DIR + "/m_file002.txt", "e")

			Thread.sleep(4000)

			modCount1 must beEqual(3)
			modCount1 must beEqual(5)
        }
    }


    private def setup {
        try { FileUtils.forceDelete(DIR) } catch { case _ => }
        FileUtils.writeStringToFile(DIR + "/m_file001.txt", "xxx")
        FileUtils.writeStringToFile(DIR + "/m_file002.txt", "xxx")
    }
}
