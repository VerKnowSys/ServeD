package com.verknowsys.served.utils


import com.verknowsys.served._

import scala.collection.mutable.Map
import scala.collection.JavaConversions._

/**
 *	Class for handling Java Properties
 *	@example
 *		val props = new Properties("config.properties")
 *		props("foo.bar.baz") getOrElse "default"
 *		props.int("foo.bar.baz") getOrElse 0
 *		props.double("foo.bar.baz") getOrElse 1.0
 *		props.bool("foo.bar.baz") getOrElse true
 *		props("foo.bar.baz") = "new value"
 *		props("foo.bar.baz") = 409
 *		props("foo.bar.baz") = true
 *	
 *	@author teamon	
 */
class Properties(filename: String) extends Utils {
	lazy val data = load

	/**
	 *	Get value as String
	 *
	 *	@author teamon
	 */
	def apply(key: String) = data.flatMap(_ get key)

	/**
	 *	Get value as Int
	 *
	 *	@author teamon
	 */
	def int(key: String) = apply(key).flatMap { s =>
		try { Some(s.toInt) } catch { case _ => None }
	}

	/**
	 *	Get value as Double
	 *
	 *	@author teamon
	 */
	def double(key: String) = apply(key).flatMap { s =>
		try { Some(s.toDouble) } catch { case _ => None }
	}

	/**
	 *	Get value as Boolean
	 *
	 *	@author teamon
	 */
	def bool(key: String) = apply(key).flatMap { s =>
		try { Some(s.toBoolean) } catch { case _ => None }
	}

	/**
	 *	Update value with String
	 *
	 *	@author teamon
	 */
	def update(key: String, value: String) {
		data.foreach(_(key) = value)
		save
	}

	/**
	 *	Update value with Int
	 *
	 *	@author teamon
	 */
	def update(key: String, value: Int) { update(key, value.toString) }

	/**
	 *	Update value with Double
	 *
	 *	@author teamon
	 */
	def update(key: String, value: Double) { update(key, value.toString) }

	/**
	 *	Update value with Boolean
	 *
	 *	@author teamon
	 */
	def update(key: String, value: Boolean) { update(key, value.toString) }

	/**
	 *	Loads properties file and returns Map
	 *
	 *	@author teamon
	 */
	protected def load = {
		try {
			val jprops = new java.util.Properties
			jprops.load(new java.io.FileInputStream(filename))
					
			logger.debug("Loaded file: " + filename)
			Some(jprops.entrySet.iterator.foldLeft(Map[String,String]()) { case(map, item) =>
				map += (item.getKey.toString -> item.getValue.toString)
			})
			
		} catch {
			case e: Exception => 
				logger.error("Couldn`t load file %s".format(filename))
				None
		}
	}

	/**
	 *	Saves data Map to file
	 *
	 *	@param fname filename
	 *	@author teamon
	 */
	protected def save {
		try {
			val jprops = new java.util.Properties
			data foreach { _.foreach(a => jprops.put(a._1, a._2)) }
			val file = new java.io.FileOutputStream(filename)
			jprops.store(file, "Scala Properties: " + filename)
			file.close
			logger.debug("Saved file: " + filename)
		} catch {
			case e: Exception => logger.error("Couldn`t save file %s".format(filename))
		}
	}	
}