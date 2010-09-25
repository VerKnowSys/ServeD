// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.maintainer

import com.verknowsys.served._

import scala.io.Source
import scala._
import junit.framework._
import junit.framework.Assert._


object MaintainerTest {

  def suite: Test = {
    val suite = new TestSuite(classOf[MaintainerTests])
    suite.addTestSuite(classOf[MaintainerTests])
    suite
  }

  def main(args : Array[String]) {
    junit.textui.TestRunner.run(suite)
  }
}


class MaintainerTests extends TestCase("Maintainer") {

  override def setUp {
  }

  
  def testEfficiencyOfTwoMethods = {
    
    def parseUsers(users: List[String]): List[Account] = {
      val userList = for (userLine <- users.filterNot{ _.startsWith("#") })
        yield
        userLine.split(":").foldRight(List[String]()) {
          (a, b) => (a :: b) 
        }
      userList.map{ new Account(_) }
    }

    def getUsers: List[Account] = parseUsers(Source.fromFile(Config.systemPasswdFile, "utf-8").getLines.toList)
    
    def parseUsers2(users: List[String]): List[Account] = {
        users.filterNot(_.startsWith("#")).map { line =>
            new Account(line.split(":").foldRight(List[String]()) {
                (a, b) => (a :: b) 
            })
        }
      }
    def getUsers2: List[Account] = parseUsers2(Source.fromFile(Config.systemPasswdFile, "utf-8").getLines.toList)
    
    
    getUsers // to cache content
    
    val start = (new java.util.Date).getTime
    for (i <- 0 to 5000) {
      getUsers
    }
    val stop = (new java.util.Date).getTime
    System.out.println("Result: " + (stop - start))

    
    val start1 = (new java.util.Date).getTime
    for (i <- 0 to 5000) {
      getUsers2
    }
    val stop1 = (new java.util.Date).getTime
    System.out.println("Result1: " + (stop1 - start1))
    
    
    // assertTrue()
    assertTrue(true)
  }

}
