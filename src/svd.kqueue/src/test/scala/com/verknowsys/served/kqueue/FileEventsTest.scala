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

    "KqueueWatcher" should {
        doBefore { setup }

        "catch 200 touched files" in {
            val kq = new Kqueue
            var n = 0

            1 to 200 foreach { i =>
                FileUtils.touch(DIR + "/touch_me_" + i + ".txt")
            }

            1 to 200 foreach { i =>
                new KqueueWatcher(kq, DIR + "/touch_me_" + i + ".txt", CLibrary.NOTE_ATTRIB)({
                    n += 1
                })
            }

            1 to 200 foreach { i =>
                FileUtils.touch(DIR + "/touch_me_" + i + ".txt")
            }

            Thread.sleep(1000)

            n must beEqual(200)
        }
    }


    private def setup {
        try { FileUtils.forceDelete(DIR) } catch { case _ => }
    }
}
