// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package deployer


import com.sun.tools.javac.resources.version
import xml.dtd.DocType
import xml.XML

/**
 * User: dmilith
 * Date: Jun 27, 2009
 * Time: 11:47:54 PM
 */

class JNLPSkeleton(var mainClass: String,
                   var title: String,
                   var codebaseURL: String,
                   var jnlpFileName: String,
				   var jars: Array[String],
                   var paramsJVM: String,
                   var vendor: String,
                   var homepage: String,
                   var icon: String,
                   var description: String ) {

	def this(mainClass: String,
	         title: String,
	         codebaseURL: String,
	         jnlpFileName: String,
	         jars: Array[String]) = this(mainClass, title, codebaseURL, jnlpFileName, jars, "", "", "", "", "")

	def this(mainClass: String,
	         title: String,
	         codebaseURL: String,
	         jnlpFileName: String,
	         jars: Array[String],
	         paramsJVM: String) = this(mainClass, title, codebaseURL, jnlpFileName, jars, paramsJVM, "", "", "", "")

	def this(mainClass: String,
	         title: String,
	         codebaseURL: String,
	         jnlpFileName: String,
	         jars: Array[String],
	         paramsJVM: String,
	         vendor: String) = this(mainClass, title, codebaseURL, jnlpFileName, jars, paramsJVM, vendor, "", "", "")

	def this(mainClass: String,
	         title: String,
	         codebaseURL: String,
	         jnlpFileName: String,
	         jars: Array[String],
	         paramsJVM: String,
	         vendor: String,
	         homepage: String) = this(mainClass, title, codebaseURL, jnlpFileName, jars, paramsJVM, vendor, homepage, "", "")

	def this(mainClass: String,
	         title: String,
	         codebaseURL: String,
	         jnlpFileName: String,
	         jars: Array[String],
	         paramsJVM: String,
	         vendor: String,
	         homepage: String,
	         icon: String) = this(mainClass, title, codebaseURL, jnlpFileName, jars, paramsJVM, vendor, homepage, icon, "")

	def getJNLP =
		<jnlp spec="1.0+" codebase={codebaseURL} href={jnlpFileName}>
		    <information>
		        <title>{title}</title>
		        <vendor>{vendor}</vendor>
		        <description>{description}</description>
				<homepage href={homepage}/>
		        <icon href={icon} kind="default"/>
		        <offline-allowed/>
		    </information>
		    <security>
		        <all-permissions/>
		    </security>
			<resources>
		        <j2se version="1.6+" java-vm-args={ paramsJVM } />
				{
					for ( jar <- jars)
				    yield
						<jar href={ "lib/" + jar } download="eager"/>
				}
		    </resources>
		    <application-desc main-class={mainClass}>
		    </application-desc>
		</jnlp>;

	def saveJNLP(file: String) = XML.saveFull(file, getJNLP, "UTF-8", true, null)
}