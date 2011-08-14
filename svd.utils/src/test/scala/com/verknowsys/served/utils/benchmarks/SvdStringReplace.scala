package com.verknowsys.served.utils.benchmarks

import com.verknowsys.served.utils._
import com.verknowsys.served.utils.Benchmark._


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
        benchmark(100000){
            report("format "){ "%s and %s".format("aaa", "bbb") } ::
            report("replace"){ "%{a} and %{b}" % ("a" -> "aaa", "b" -> "bbb") } ::
            Nil
        }
    }
}
