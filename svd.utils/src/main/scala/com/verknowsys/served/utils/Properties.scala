package com.verknowsys.served.utils

import scala.collection.mutable.Map
import scala.collection.JavaConversions._
import java.io.{FileInputStream, FileOutputStream}
import java.util.{Properties => JProperties}

abstract class PropertyConverter[T, Property] {
    def apply(s: String): Option[T]
}

object Property {
    implicit object StringPropertyConverter extends PropertyConverter[String, Property] {
        def apply(s: String) = Some(s)
    }

    implicit object IntPropertyConverter extends PropertyConverter[Int, Property] {
        def apply(s: String) = try { Some(s.toInt) } catch { case e:java.lang.NumberFormatException => None }
    }

    implicit object DoublePropertyConverter extends PropertyConverter[Double, Property] {
        def apply(s: String) = try { Some(s.toDouble) } catch { case e:java.lang.NumberFormatException => None }
    }

    implicit object BooleanPropertyConverter extends PropertyConverter[Boolean, Property] {
        def apply(s: String) = try { Some(s.toBoolean) } catch { case e:java.lang.NumberFormatException => None }
    }
}

class Property(parent: Properties, key: String){
    lazy val value = parent.data.flatMap(_ get key)
    
    def or[T](default: T)(implicit conv: PropertyConverter[T, Property]):T = value.flatMap(conv(_)) getOrElse {
        parent(key) = default.toString
        default
    }    
}


/**
 * 	Class for handling Java Properties
 * @example
 * val props = new Properties("config.properties")
 * 		props("foo.bar.baz") getOrElse "default"
 * 		props.int("foo.bar.baz") getOrElse 0
 * 		props.double("foo.bar.baz") getOrElse 1.0
 * 		props.bool("foo.bar.baz") getOrElse true
 * 		props("foo.bar.baz") = "new value"
 * 		props("foo.bar.baz") = 409
 * 		props("foo.bar.baz") = true
 *
 * @author teamon
 */
class Properties(filename: String) extends Utils {
    lazy val data = load

    /**
     * 	Get value as Option[String]
     *
     * @author teamon
     */
    def apply(key: String): Option[String] = {
        val value = data.flatMap(_ get key)
        logger.trace("Setting props(%s) with value %s".format(key,value))
        value
    }
    
    
    def get(key: String) = new Property(this, key)
    
    /**
     * 	Get value as String
     *  
     *   If key doesnt exist it saves the default value
     *  
     * @author teamon
     */
    def apply(key: String, default: String): String = apply(key) getOrElse {
        update(key, default)
        default    
    }

    /**
     * 	Get value as Option[Int]
     *
     * @author teamon
     */
    def int(key: String): Option[Int] = apply(key).flatMap { s => try { Some(s.toInt) } catch { case _ => None } }
    
    /**
     * Get value as Int
     *  
     *   If key doesnt exist it saves the default value
     *
     * @author teamon
     */
    def int(key: String, default: Int): Int = int(key) getOrElse { 
        update(key, default)
        default 
    }

    /**
     * 	Get value as Option[Double]
     *
     * @author teamon
     */
    def double(key: String): Option[Double] = apply(key).flatMap { s => try { Some(s.toDouble) } catch { case _ => None } }

    /**
     * Get value as Double
     *  
     *   If key doesnt exist it saves the default value
     *
     * @author teamon
     */
    def double(key: String, default: Double): Double = double(key) getOrElse { 
        update(key, default)
        default 
    }

    /**
     * 	Get value as Option[Boolean]
     *
     * @author teamon
     */
    def bool(key: String): Option[Boolean] = apply(key).flatMap { s => try { Some(s.toBoolean) } catch { case _ => None } }

    /**
     * Get value as Boolean
     *  
     *   If key doesnt exist it saves the default value
     *
     * @author teamon
     */
    def bool(key: String, default: Boolean): Boolean = bool(key) getOrElse { 
        update(key, default)
        default 
    }

    /**
     * 	Update value with String
     *
     * @author teamon
     */
    def update(key: String, value: String) {
        data.foreach(_(key) = value)
        save
    }

    /**
     * 	Update value with Int
     *
     * @author teamon
     */
    def update(key: String, value: Int) { update(key, value.toString) }

    /**
     * 	Update value with Double
     *
     * @author teamon
     */
    def update(key: String, value: Double) { update(key, value.toString) }

    /**
     * 	Update value with Boolean
     *
     * @author teamon
     */
    def update(key: String, value: Boolean) { update(key, value.toString) }

    /**
     * 	Loads properties file and returns Map
     *
     * @author teamon
     */
    protected def load = {
        try {
            val jprops = new JProperties
            jprops.load(new FileInputStream(filename))

            logger.debug("Loaded file: " + filename)
            Some(jprops.entrySet.iterator.foldLeft(Map[String, String]()) {
                case (map, item) =>
                    map += (item.getKey.toString -> item.getValue.toString)
            })
            
        } catch {
            case e: Exception =>
                logger.error("Couldn`t load file %s".format(filename))
                None
        }
    }

    /**
     * 	Saves data Map to file
     *
     * @param fname filename
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
            logger.debug("Saved file: " + filename)
        // } catch {
            // case e: Exception => logger.error("Couldn`t save file %s, cause of exception: %s".format(filename, e))
        // }
    }
    
}
