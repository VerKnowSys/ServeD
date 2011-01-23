package com.verknowsys.served.utils.benchmarks

import com.verknowsys.served.utils._
import com.verknowsys.served.utils.SvdBenchmark


/** 
 * Simple benchmark for string replace
 *
 * Run with: 
 * {{{
 * > test-run com.verknowsys.served.utils.benchmarks.StringReplace
 * }}}
 * 
 * @author teamon
 */
object SvdStringReplace {
    def main(args: Array[String]): Unit = {
        SvdBenchmark(100000){ b =>
            b("format "){ "%s and %s".format("aaa", "bbb") }
            b("replace"){ "%{a} and %{b}" % ("a" -> "aaa", "b" -> "bbb") }
        }
    }
}
