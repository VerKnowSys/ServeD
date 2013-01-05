/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.utils

import scala.collection.mutable.Map
import scala.collection.JavaConversions._
import java.io.{FileInputStream, FileOutputStream}
import java.util.{Properties => JProperties}
import org.apache.commons.io.FileUtils


/**
 * SvdProperty converter interface
 *
 * @author teamon
 */
abstract class PropertyConverter[T, SvdProperty] {
    /**
     * Convert string into T-type
     *
     * @author teamon
     */
    def apply(s: String): Option[T]

    /**
     * Convert T-type into String
     *
     * @author teamon
     */
    def toString(a: T): String
}

/**
 * Object holding implicit converters
 *
 * @author teamon
 */
object SvdProperty {
    implicit object StringPropertyConverter extends PropertyConverter[String, SvdProperty] {
        def apply(s: String) = Some(s)
        def toString(s: String) = s
    }

    implicit object IntPropertyConverter extends PropertyConverter[Int, SvdProperty] {
        def apply(s: String) = try { Some(s.toInt) } catch { case e:java.lang.NumberFormatException => None }
        def toString(i: Int) = i.toString
    }

    implicit object DoublePropertyConverter extends PropertyConverter[Double, SvdProperty] {
        def apply(s: String) = try { Some(s.toDouble) } catch { case e:java.lang.NumberFormatException => None }
        def toString(d: Double) = d.toString
    }

    implicit object BooleanPropertyConverter extends PropertyConverter[Boolean, SvdProperty] {
        def apply(s: String) = try { Some(s.toBoolean) } catch { case e:java.lang.NumberFormatException => None }
        def toString(b: Boolean) = b.toString
    }
}

/**
 * Single property representation
 *
 * @author teamon
 */
class SvdProperty(parent: SvdProperties, key: String){
    lazy val value = parent.data.flatMap(_ get key)

    def or[T](default: T)(implicit conv: PropertyConverter[T, SvdProperty]):T = value.flatMap(conv(_)) getOrElse {
        parent(key) = default.toString
        default
    }
}


/**
 * Class for handling Java Properties
 *
 * @example
 * val props = new Properties("config.properties")
 *      props("foo.bar.baz") or "default"
 *      props("foo.bar.baz") or 0
 *      props("foo.bar.baz") or 1.0
 *      props("foo.bar.baz") or true
 *      props("foo.bar.baz") = "new value"
 *      props("foo.bar.baz") = 409
 *      props("foo.bar.baz") = true
 *
 * @author teamon
 */
class SvdProperties(filename: String) extends SvdUtils {
    lazy val data = load

    /**
     *  Get value as SvdProperty object
     *
     * @author teamon
     */
    def apply(key: String) = new SvdProperty(this, key)

    /**
     * Update valueonverters
     *
     * @author teamon
     */
    def update[T](key: String, value: T)(implicit conv: PropertyConverter[T, SvdProperty]){
        data.foreach(_(key) = conv.toString(value))
        save
    }

    /**
     * Remove key
     *
     * @author teamon
     */
    def remove(key: String){
        data.foreach(_ -= key)
        save
    }

    /**
     * Loads properties file and returns Map
     *
     * @author teamon
     */
    protected def load = {
        try {
            val jprops = new JProperties
            FileUtils.touch(filename) // 2011-01-27 01:12:08 - dmilith - NOTE: this is required to avoid first time run failures
            jprops.load(new FileInputStream(filename))

            Some(jprops.entrySet.iterator.foldLeft(Map[String, String]()) {
                case (map, item) =>
                    map += (item.getKey.toString -> item.getValue.toString)
            })

        } catch {
            case e: Exception =>
                // log.error("Could not read file %s, cause of exception: %s".format(filename, e))
                None
        }
    }

    /**
     * Saves data Map to file
     *
     * @author teamon
     */
    protected def save {
        // try {
            val jprops = new JProperties
            val file = new FileOutputStream(filename)
            try {
                data foreach {_.foreach(a => jprops.put(a._1, a._2))}
            } catch {
                case x: Exception =>
                    log.debug("Exception while storing property: %s" + x)
            }
            jprops.store(file, "ServeD Properties: " + filename)
            file.close
            // log.debug("Saved file: " + filename)
        // } catch {
            // case e: Exception => error("Couldn`t save file %s, cause of exception: %s".format(filename, e))
        // }
    }

}