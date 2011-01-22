package com.verknowsys.served.utils

import scala.collection.mutable.Map
import scala.collection.JavaConversions._
import java.io.{FileInputStream, FileOutputStream}
import java.util.{Properties => JProperties}
import akka.util.Logging

/** 
 * Property converter interface
 *   
 * @author teamon
 */
abstract class PropertyConverter[T, Property] {
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
object Property {
    implicit object StringPropertyConverter extends PropertyConverter[String, Property] {
        def apply(s: String) = Some(s)
        def toString(s: String) = s
    }

    implicit object IntPropertyConverter extends PropertyConverter[Int, Property] {
        def apply(s: String) = try { Some(s.toInt) } catch { case e:java.lang.NumberFormatException => None }
        def toString(i: Int) = i.toString
    }

    implicit object DoublePropertyConverter extends PropertyConverter[Double, Property] {
        def apply(s: String) = try { Some(s.toDouble) } catch { case e:java.lang.NumberFormatException => None }
        def toString(d: Double) = d.toString
    }

    implicit object BooleanPropertyConverter extends PropertyConverter[Boolean, Property] {
        def apply(s: String) = try { Some(s.toBoolean) } catch { case e:java.lang.NumberFormatException => None }
        def toString(b: Boolean) = b.toString
    }
}

/** 
 * Single property representation
 *   
 * @author teamon
 */
class Property(parent: Properties, key: String){
    lazy val value = parent.data.flatMap(_ get key)
    
    def or[T](default: T)(implicit conv: PropertyConverter[T, Property]):T = value.flatMap(conv(_)) getOrElse {
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
class Properties(filename: String) extends Logging {
    lazy val data = load

    /**
     *  Get value as Property object
     *
     * @author teamon
     */
    def apply(key: String) = new Property(this, key)
    
    /** 
     * Update valueonverters
     *   
     * @author teamon
     */
    def update[T](key: String, value: T)(implicit conv: PropertyConverter[T, Property]){
        data.foreach(_(key) = conv.toString(value))
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
            jprops.load(new FileInputStream(filename))

            log.debug("Loaded file: " + filename)
            Some(jprops.entrySet.iterator.foldLeft(Map[String, String]()) {
                case (map, item) =>
                    map += (item.getKey.toString -> item.getValue.toString)
            })
            
        } catch {
            case e: Exception =>
                error("Could not save file %s, cause of exception: %s".format(filename, e))
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
                case _ => // 2010-10-02 00:35:14 - dmilith -  XXX: silent fail on first save
            }
            jprops.store(file, "ServeD Properties: " + filename)
            file.close
            log.debug("Saved file: " + filename)
        // } catch {
            // case e: Exception => error("Couldn`t save file %s, cause of exception: %s".format(filename, e))
        // }
    }
    
}
