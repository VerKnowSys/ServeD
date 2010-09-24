// // © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// // This Software is a close code project. You may not redistribute this code without permission of author.
// 
// package com.verknowsys.served.utils.deployer
// 
// import junit.framework._
// import skeletons.JNLPSkeleton;
// import Assert._;
// 
// object JNLPSkeletonTest {
// 
//     def suite: Test = {
//         val suite = new TestSuite(classOf[JNLPSkeletonTest]);
//         suite
//     }
// 
//     def main(args : Array[String]) {
//         junit.textui.TestRunner.run(suite);
//     }
// }
// 
// class JNLPSkeletonTest extends TestCase("app") {
// 
// 	def testJnlpCreation = {
// 		val z = new JNLPSkeleton("main.Class", "title", "http://codebase.url/", "myShinyJNLPFile.jnlp",
// 			List("my1.jar","my2.jar","additional.jar", "last.jar"), "param1 param2 param3 param666",
// 			"ME - the vendor", "http://my.home.com/", "myShinyIcon.ico", "This is my description")
// 		println(z.getJNLP)
// 		assertTrue(z.getJNLP.toString.length > 0)
// 		val myXML = {
// 			<jnlp href="myShinyJNLPFile.jnlp" codebase="http://codebase.url/" spec="1.0+">
// 		    <information>
// 		        <title>title</title>
// 		        <vendor>ME - the vendor</vendor>
// 		        <description>This is my description</description>
// 				<homepage href="http://my.home.com/"></homepage>
// 		        <icon href="myShinyIcon.ico" kind="default"></icon>
// 		        <offline-allowed></offline-allowed>
// 		    </information>
// 		    <security>
// 		        <all-permissions></all-permissions>
// 		    </security>
// 			<resources>
// 		        <j2se java-vm-args="param1 param2 param3 param666" version="1.6+"></j2se>
// 				<jar download="eager" href="lib/my1.jar"></jar><jar download="eager" href="lib/my2.jar"></jar><jar download="eager" href="lib/additional.jar"></jar><jar download="eager" href="lib/last.jar"></jar>
// 		    </resources>
// 		    <application-desc main-class="main.Class">
// 		    </application-desc>
// 		</jnlp>
// 		}
// 		assertTrue(myXML == z.getJNLP)
// 		z.saveJNLP("/tmp/file.jnlp")
// 	}
// 
// }
