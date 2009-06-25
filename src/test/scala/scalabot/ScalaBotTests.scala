// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package scalabot.tests

import scalabot._

import scala.collection.mutable.HashMap
import scala._

import junit.framework._
import junit.framework.Assert.assertEquals 
import junit.framework.Assert.fail 
import junit.textui._

import java.util.Date
import java.util.ArrayList
import java.io.File


class CommitTest1 extends TestCase {
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
}


class ConfigTest1 extends TestCase {
	var settings = new Preferences("")
	
	override def setUp {
		settings.value("configFile") = "testing.config"
	}

	def testXML2 = {
		settings.value("port") = 666
		assert( settings.loadPreferences != null )
		println( settings.geti("port") )
		// println( settings.loadPreferences.geti("port") )
	//	settings = settings.loadPreferences
	}
	
	def testConfigReadWrite = {
		assert(settings.get("configFile").equals("testing.config"))
		try {
			settings.savePreferences(settings.get("configFile"))
		} catch {
			case _ => {
				fail("Cannot save preferences file!")
			}
		}
		try {
			settings.loadPreferences
		} catch {
			case _ => {
				fail("Cannot load preferences file!")
			}
		}
	}
	
	def testConfigSettings = {
		assert(settings.get("configFile").equals("testing.config"))
		settings.value("repositoryDir") = "/something"
		assert(settings.get("repositoryDir").equals("/something"))
	}
	
}


object ScalaBotTests extends TestCase {
	
	def main (args: Array[String]) = {
		TestRunner.run(suite)
	}

	def suite: Test = {
		val suite = new TestSuite
		suite.addTestSuite(classOf[CommitTest1])
		suite.addTestSuite(classOf[ConfigTest1])
		suite
	}
	
} 
