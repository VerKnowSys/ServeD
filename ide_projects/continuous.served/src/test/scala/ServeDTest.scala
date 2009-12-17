// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package scalabot

import scala._
import junit.framework._
import junit.framework.Assert._


object ServeDTest {

	def suite: Test = {
        val suite = new TestSuite(classOf[ServeDTests])
		suite.addTestSuite(classOf[ServeDTests])
        suite
    }

    def main(args : Array[String]) {
        junit.textui.TestRunner.run(suite);
    }
}


class ServeDTests extends TestCase("ServeD") {

	override def setUp {

	}

	def testExample = {
		assertTrue(true)
	}

}
