// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package scalabot

import commiter.Commit
import org.junit.Ignore
import prefs.Preferences
import scalabot._

import scala._

import junit.framework._;
import Assert._;

object ScalaBotTests {

	def suite: Test = {
        val suite = new TestSuite(classOf[CommitTest])
		suite.addTestSuite(classOf[ConfigTest])
        suite
    }

    def main(args : Array[String]) {
        junit.textui.TestRunner.run(suite);
    }
}

class CommitTest extends TestCase("commit") {
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
		assertTrue( commit != null )
	}
}


class ConfigTest extends TestCase("config") {
//	var settings: Preferences = _

	override def setUp {
//		settings = new Preferences
	}

	def testXML2 = {
//		settings.value("xmppPort") = 666
//		assertTrue( settings.loadPreferences != null )
//		println( settings.geti("xmppPort") )
		assertTrue(true)
	}
//
//	def testConfigReadWrite = {
//		try {
////			settings.savePreferences("testConfig.xml")
//		} catch {
//			case _ => {
//				fail("Cannot save preferences file!")
//			}
//		}
//		try {
//			settings.loadPreferences
//		} catch {
//			case _ => {
//				fail("Cannot load preferences file!")
//			}
//		}
//	}

//	def testConfigSettings = {
//		assertTrue(settings.get("configFile").equals("testing.config"))
//		settings.value("gitRepositoryProjectDir") = "/something"
//		assertTrue(settings.get("gitRepositoryProjectDir").equals("/something"))
//	}
	
}