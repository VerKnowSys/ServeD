package com.verknowsys.served.tools

import scala.collection.mutable.Map
import scala.collection.JavaConversions._

/**
 * Class for handling Java Properties
 *	Example usage:
 *		val props = new Properties("config.properties")
 *		props("foo.bar.baz") getOrElse "default"
 *		props("foo.bar.baz") = "new value"
 *	
 *	@author teamon	
 */
class Properties(filename: String){
	val data = loadFile(filename)
	
	def apply(key: String) = data.flatMap(_ get key)
	
	def update(key: String, value: String) {
		data.foreach(_(key) = value)
		save
	}
	
	protected def loadFile(fname: String) = {
		try {
			val jprops = new java.util.Properties
			jprops.load(new java.io.FileInputStream(fname))
					
			Some(jprops.entrySet.iterator.foldLeft(Map[String,String]()) { case(map, item) =>
				map += (item.getKey.toString -> item.getValue.toString)
			})
			
		}
		catch {
			case e: Exception => None
		}
	}
	
	protected def save {
		try {
			val jprops = new java.util.Properties
			data foreach { _.foreach(a => jprops.put(a._1, a._2)) }
			val file = new java.io.FileOutputStream(filename)
			jprops.store(file, "Scala Properties: " + filename)
			file.close
		} catch {
			case e: Exception => println("[ERROR] Properties.save: " + e)
		}
	}	
}