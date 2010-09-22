// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.maintainer

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
        junit.textui.TestRunner.run(suite);
    }
}


class MaintainerTests extends TestCase("Maintainer") {

	override def setUp {

	}

	def testExample = {
		assertTrue(true)
	}

}
