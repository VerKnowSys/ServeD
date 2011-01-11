package com.verknowsys.served

import java.io.File

/**
 * Package object holding implicit conversions for utils packge
 *
 * @author teamon
 */
package object utils {
    /**
     * Convert string into File object
     *
     * @author teamon
     */
    implicit def StringToFile(s: String) = new File(s)
    
    
    implicit object StringPropertyConverter extends PropertyConverter[String] {
        def apply(s: String) = s
    }

    implicit object IntPropertyConverter extends PropertyConverter[Int] {
        def apply(s: String) = s.toInt
    }

    implicit object DoublePropertyConverter extends PropertyConverter[Double] {
        def apply(s: String) = s.toDouble
    }

    implicit object BooleanPropertyConverter extends PropertyConverter[Boolean] {
        def apply(s: String) = s.toBoolean
    }
}
