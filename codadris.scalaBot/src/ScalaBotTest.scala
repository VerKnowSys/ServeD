// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.

package scalabot.tests

import junit.framework._
import junit.framework.Assert.assertEquals 
import junit.framework.Assert.fail 
import junit.textui._

class Test1 extends TestCase {

	var commit: Commit = null
	
	override def setUp {
		commit = null
	}
	
	def testCommitCreation = {
		try {
			commit = new Commit("")
			fail
		} catch {
			case a: Throwable => {
			}
		}
	}
	
	def testCommitCreation2 = {
		commit = new Commit("Something")
		assert( commit != null )	
	}
	
		// try {
		// 	fail()
		// } catch {
		// 	case e: IllegalArgumentException => // expected
		// }
	
}

object ScalaBotTest extends TestCase {
	
	def main (args: Array[String]) = {
		TestRunner.run(suite)
	}

	def suite: Test = {
		val suite = new TestSuite
		suite.addTestSuite(classOf[Test1])
		suite
	}
	
} 
