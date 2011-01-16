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
    
    /**
     * Replace string usign key-value
     * 
     * {{{
     *    scala> "foo %{bar} and %{baz}" % ("bar" -> "xxx", "baz" -> "blah")
     *    res0: String = foo xxx and blah
     * }}}
     *
     * @author teamon
     */
    implicit def repl4str(s: String) = new {
        def %(args: Any*) = args.foldLeft(s){ case (s, a) => a match {
            case (key, value) => s.replace("%{" + key + "}", value.toString) 
            case value => s.replaceFirst("%", value.toString)
        } }
    }
    
}
