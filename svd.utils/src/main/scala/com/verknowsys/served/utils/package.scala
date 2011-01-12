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

}
