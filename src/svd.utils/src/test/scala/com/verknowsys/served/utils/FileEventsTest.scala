package com.verknowsys.served.utils

import org.specs._
import java.io._
import org.apache.commons.io.FileUtils
import scala.collection.mutable.ListBuffer


class FileEventsTest extends SpecificationWithJUnit {
    final val DIR = "/tmp/served/file_events_test/"

    "FileWatcher" should {
        doBefore { setup }

        // this is run in one singe example due to necessary waiting
        // sleeping for at least 2-3 seconds in each example 
        // will slow down tests dramatically
        "notify ..." in {
            val watchAll = new FileWatcher(DIR) {
                val createdList = ListBuffer[String]()
                val modifiedList = ListBuffer[String]()
                val deletedList = ListBuffer[String]()

                override def created(name: String) {
                    createdList += name
                }

                override def modified(name: String) {
                    modifiedList += name
                }

                override def deleted(name: String) {
                    deletedList += name
                }
            }

            val watchAllRecursive = new FileWatcher(DIR, recursive = true) {
                val createdList = ListBuffer[String]()
                val modifiedList = ListBuffer[String]()
                val deletedList = ListBuffer[String]()

                override def created(name: String) {
                    createdList += name
                }

                override def modified(name: String) {
                    modifiedList += name
                }

                override def deleted(name: String) {
                    deletedList += name
                }
            }

            val watchCreatedList = ListBuffer[String]()
            val watchCreated = FileEvents.watchCreated(DIR) {
                name => watchCreatedList += name
            }

            val watchModifiedList = ListBuffer[String]()
            val watchModified = FileEvents.watchModified(DIR) {
                name => watchModifiedList += name
            }

            val watchDeletedList = ListBuffer[String]()
            val watchDeleted = FileEvents.watchDeleted(DIR) {
                name => watchDeletedList += name
            }

            Thread.sleep(1000) // startup delay

            FileUtils.writeStringToFile(DIR + "/c_file001.txt", "...")
            FileUtils.writeStringToFile(DIR + "/c_file002.txt", "...")
            FileUtils.writeStringToFile(DIR + "/subdir/and/one/more/c_file001.txt", "...")
            FileUtils.writeStringToFile(DIR + "/subdir/and/one/more/c_file002.txt", "...")
            FileUtils.writeStringToFile(DIR + "/m_file001.txt", "123")
            FileUtils.writeStringToFile(DIR + "/m_file002.txt", "321")
            FileUtils.writeStringToFile(DIR + "/subdir/and/one/more/m_file001.txt", "123")
            FileUtils.writeStringToFile(DIR + "/subdir/and/one/more/m_file002.txt", "321")
            FileUtils.forceDelete(DIR + "/d_file001.txt")
            FileUtils.forceDelete(DIR + "/d_file002.txt")
            FileUtils.forceDelete(DIR + "/subdir/and/one/more/d_file001.txt")
            FileUtils.forceDelete(DIR + "/subdir/and/one/more/d_file002.txt")

            Thread.sleep(1000)

            watchAll.createdList must contain("c_file001.txt")
            watchAll.createdList must contain("c_file002.txt")
            watchAll.createdList must haveSize(2)

            watchAll.modifiedList must contain("m_file001.txt")
            watchAll.modifiedList must contain("m_file002.txt")
            watchAll.modifiedList must haveSize(2)

            watchAll.deletedList must contain("d_file001.txt")
            watchAll.deletedList must contain("d_file002.txt")
            watchAll.deletedList must haveSize(2)


            watchAllRecursive.createdList must contain("c_file001.txt")
            watchAllRecursive.createdList must contain("c_file002.txt")
            watchAllRecursive.createdList must contain("subdir/and/one/more/c_file001.txt")
            watchAllRecursive.createdList must contain("subdir/and/one/more/c_file002.txt")
            watchAllRecursive.createdList must haveSize(4)

            watchAllRecursive.modifiedList must contain("m_file001.txt")
            watchAllRecursive.modifiedList must contain("m_file002.txt")
            watchAllRecursive.modifiedList must contain("subdir/and/one/more/m_file001.txt")
            watchAllRecursive.modifiedList must contain("subdir/and/one/more/m_file002.txt")
            watchAllRecursive.modifiedList must haveSize(4)

            watchAllRecursive.deletedList must contain("d_file001.txt")
            watchAllRecursive.deletedList must contain("d_file002.txt")
            watchAllRecursive.deletedList must contain("subdir/and/one/more/d_file001.txt")
            watchAllRecursive.deletedList must contain("subdir/and/one/more/d_file002.txt")
            watchAllRecursive.deletedList must haveSize(4)


            watchCreatedList must contain("c_file001.txt")
            watchCreatedList must contain("c_file002.txt")
            watchCreatedList must haveSize(2)

            watchModifiedList must contain("m_file001.txt")
            watchModifiedList must contain("m_file002.txt")
            watchModifiedList must haveSize(2)

            watchDeletedList must contain("d_file001.txt")
            watchDeletedList must contain("d_file002.txt")
            watchDeletedList must haveSize(2)

            watchAll.stop
            watchCreated.stop
            watchModified.stop
            watchDeleted.stop
        }
    }


    private def setup {
        try { FileUtils.forceDelete(DIR) } catch { case _ => }
        FileUtils.forceMkdir(DIR + "/subdir/and/one/more")
        FileUtils.writeStringToFile(DIR + "/m_file001.txt", "xxx")
        FileUtils.writeStringToFile(DIR + "/m_file002.txt", "xxx")
        FileUtils.writeStringToFile(DIR + "/subdir/and/one/more/m_file001.txt", "xxx")
        FileUtils.writeStringToFile(DIR + "/subdir/and/one/more/m_file002.txt", "xxx")
        FileUtils.writeStringToFile(DIR + "/d_file001.txt", "xxx")
        FileUtils.writeStringToFile(DIR + "/d_file002.txt", "xxx")
        FileUtils.writeStringToFile(DIR + "/subdir/and/one/more/d_file001.txt", "xxx")
        FileUtils.writeStringToFile(DIR + "/subdir/and/one/more/d_file002.txt", "xxx")
    }
}
