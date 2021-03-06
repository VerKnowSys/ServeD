/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

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
     * Join two paths into one
     *
     * {{{
     * "foo/bar"  / "baz"  // "foo/bar/baz"
     * "foo/bar/" / "baz"  // "foo/bar/baz"
     * "foo/bar"  / "/baz" // "foo/bar/baz"
     * "foo/bar/" / "/baz" // "foo/bar/baz"
     * }}}
     *
     * @author teamon
     */
    implicit def StringToPath(a: String) = new {
        def /(b: String) = {
            if(a.isEmpty) b
            else if(b.isEmpty) a
            else if(a.last == '/'){
                if(b.head == '/') a + b.substring(1)
                else a + b
            } else {
                if(b.head == '/') a + b
                else a + '/' + b
            }
        }
    }

    /**
     * Replace string using key-value
     *
     * {{{
     *    scala> "foo %{bar} and %{baz}" % ("bar" -> "xxx", "baz" -> "blah")
     *    res0: String = foo xxx and blah
     * }}}
     *
     * @author teamon
     */
    implicit def repl4str(s: String) = new {
        def %(args: Any*) = args.foldLeft(s) {
            case (s, a) => a match {
                case (key, value) => s.replace("%{" + key + "}", value.toString)
                case value => s.replaceFirst("%", value.toString)
            }
        }
    }

    /**
     * Executes given call-by-name and returns Some(val)
     * In case of any exception returns None
     *
     * @author teamon
     */
    def catchException[T](f: => T): Option[T] = try {
        Some(f)
    } catch {
        case x: Exception => None
    }

    // implicit def GlobalActorRef2ActorRef(global: GlobalActorRef) = global.actor

}
