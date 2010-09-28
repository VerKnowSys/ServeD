package com.verknowsys.served.utils

import org.specs._
import java.io._
import org.apache.commons.io.FileUtils
import scala.collection.mutable.ListBuffer

class FileEventsTest extends SpecificationWithJUnit {
    final val DIR = "/tmp/served/file_events_test"
    
    "FileWatcher" should { 
        doBefore { setup }
                
        // this is run in one singe example due to necessary waiting
        // sleeping for at least 2-3 seconds in each example 
        // will slow down tests dramatically
        "notify ..." in {
            val watchAll = new FileWatcher(DIR){
                val createdList = ListBuffer[String]()
                val modifiedList = ListBuffer[String]()
                val deletedList = ListBuffer[String]()
                
                override def created(name: String){
                    createdList += name
                }
                
                override def modified(name: String){
                    modifiedList += name
                }
                
                override def deleted(name: String){
                    deletedList += name
                }
            }
            
            
            FileUtils.writeStringToFile(DIR + "/c_file001.txt", "...")
            FileUtils.writeStringToFile(DIR + "/c_file002.txt", "...")
            FileUtils.writeStringToFile(DIR + "/m_file001.txt", "...")
            FileUtils.writeStringToFile(DIR + "/m_file002.txt", "...")
            FileUtils.forceDelete(DIR + "/d_file001.txt")
            FileUtils.forceDelete(DIR + "/d_file002.txt")
            
            Thread.sleep(5000)
            
            watchAll.createdList must contain("c_file001.txt")
            watchAll.createdList must contain("c_file002.txt")
            watchAll.modifiedList must contain("m_file001.txt")
            watchAll.modifiedList must contain("m_file002.txt")
            watchAll.deletedList must contain("d_file001.txt")
            watchAll.deletedList must contain("d_file002.txt")
            
        }
    }
    

    private def setup {
        FileUtils.deleteDirectory(DIR)
        FileUtils.forceMkdir(DIR)
        FileUtils.writeStringToFile(DIR + "/m_file001.txt", "xxx")
        FileUtils.writeStringToFile(DIR + "/m_file002.txt", "xxx")
        FileUtils.writeStringToFile(DIR + "/d_file001.txt", "xxx")        
        FileUtils.writeStringToFile(DIR + "/d_file002.txt", "xxx")
    }
}