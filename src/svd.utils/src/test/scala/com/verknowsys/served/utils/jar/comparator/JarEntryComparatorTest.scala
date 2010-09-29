// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.utils.jar.comparator


import junit.framework._;
import Assert._;


object JarEntryComparatorTest {

    def suite: Test = {
        val suite = new TestSuite(classOf[JarEntryComparatorTest]);
        suite
    }

    def main(args : Array[String]) {
        junit.textui.TestRunner.run(suite);
    }
}

/**
 * Unit test for simple App.
 */
class JarEntryComparatorTest extends TestCase("app") {

	var z: JarEntryComparator = _
	var currentDir: String = _

	def prepare = {
		z = new JarEntryComparator
	    currentDir = System.getProperty("user.dir") + "/src/test/scala/com/verknowsys/served/utils/jar/comparator/test_data/"
	    println(currentDir)
	}

	def checkAllAdditionalStandardAssertions = {
	    assertTrue(z.diff != null)
	    if (z.diff == List() && z.size == z.size2) assertFalse(z.diff_?)
	    if (z.diff_?) assertTrue(z.diff != List() || z.size != z.size2)
		assertTrue(z.size > 0)
		println("First: " + z.size + "\t" + z.elements)
		println("Second: " + z.size2 + "\t" + z.elements2)
		println("Differs: " + z.diff_?)
	}

    def testJarsWithNoDifferences = {
	    prepare
	    try {
	        z.load(currentDir + "reference_test_file.zip",
		        currentDir + "identical_to_reference_test_file.zip")
	    } catch {
		    case x: Exception => {
			    println("Error while loading test jars. Test skipped!")
			    x.printStackTrace
			    exit
		    }
	    }
	    assertFalse(z.diff_?) // No difference in one same file
	    checkAllAdditionalStandardAssertions
    }

	def testJarsWithLackOfOneFileDifference = {
	    prepare
	    try {
	        z.load(currentDir + "one_file_added.zip",
		        currentDir + "reference_test_file.zip")
	    } catch {
		    case x: Exception => {
			    println("Error while loading test jars. Test skipped!")
			    x.printStackTrace
			    exit
		    }
	    }
	    assertTrue(z.diff_?)
	    checkAllAdditionalStandardAssertions
    }
	
	def testJarsWithLackOfOneFileDifferenceAndChange = {
	    prepare
	    try {
	        z.load(currentDir + "one_file_added_deleted_and_changed.zip",
		        currentDir + "reference_test_file.zip")
	    } catch {
		    case x: Exception => {
			    println("Error while loading test jars. Test skipped!")
			    x.printStackTrace
			    exit
		    }
	    }
	    assertTrue(z.diff_?)
	    checkAllAdditionalStandardAssertions
    }

	def testJarsWithOneFileChanged = {
	    prepare
	    try {
	        z.load(currentDir + "one_file_changed.zip",
		        currentDir + "reference_test_file.zip")
	    } catch {
		    case x: Exception => {
			    println("Error while loading test jars. Test skipped!")
			    x.printStackTrace
			    exit
		    }
	    }
	    assertTrue(z.diff_?)
	    checkAllAdditionalStandardAssertions
    }

	def testJarsWithOneFileDeleted = {
	    prepare
	    try {
	        z.load(currentDir + "one_file_deleted.zip",
		        currentDir + "reference_test_file.zip")
	    } catch {
		    case x: Exception => {
			    println("Error while loading test jars. Test skipped!")
			    x.printStackTrace
			    exit
		    }
	    }
	    assertTrue(z.diff_?)
	    checkAllAdditionalStandardAssertions
    }

	def testJarsWith = {
	    prepare
	    try {
	        z.load(currentDir + "one_file_deleted.zip",
		        currentDir + "reference_test_file.zip")
	    } catch {
		    case x: Exception => {
			    println("Error while loading test jars. Test skipped!")
			    x.printStackTrace
			    exit
		    }
	    }
	    assertTrue(z.diff_?)
	    checkAllAdditionalStandardAssertions
    }

	def testJarsWithBrokenZip = {
	    prepare
		println("Trying with broken zip")
	    try {
	        z.load(currentDir + "broken.zip",
		        currentDir + "reference_test_file.zip")
	    } catch {
		    case x: Exception => {
			    println("Error while loading test jars. Test skipped!")
			    x.printStackTrace
			    exit
		    }
	    }
	    assertTrue(z.diff_?)
		assertTrue(z.diff == List())
		assertTrue(z.size == 0)
		assertTrue(z.size2 == 0)
		assertTrue(z.elements == Nil || z.elements2 == Nil) // one should be broken so empty List is Nil
		println("Differs: " + z.diff_?)
		assertTrue(z.loadAndThrowListOfCrcs(currentDir + "broken.zip") == List())
    }
}
