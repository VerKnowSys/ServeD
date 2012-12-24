package com.verknowsys.served.utils


/**
 * Simple benchmark utility
 * 
 * Usage
 * {{{
 *     import com.verknowsys.utils.Benchmark._
 * 
 *     benchmark(10000)(
 *         report("foo"){ foo() } ::
 *         report("bar"){ bar() } ::
 *         Nil
 *     )
 * }}}
 * 
 * will print
 * 
 * {{{
 *     Name                    Time(s)
 *     ===============================
 *     foo                    0.084000
 *     bar                    0.082000
 * }}}
 */ 
object Benchmark {
    case class BenchmarkReport(name: String, f: () => Unit){
        def run(n: Int) = {
            val start = System.currentTimeMillis
            (1 to n) foreach { i => f() }
            val time = System.currentTimeMillis - start
            (name, time)
        }
    }
    
    def benchmark(n: Int)(reports: List[BenchmarkReport]) = {
        val results = reports.map(_.run(n))
        println("Name                    Time(s)")
        println("===============================")
        results.foreach { case (name, time) =>
            printf("%-20s %10f\n", name, time / 1000.0)
        }
        results
    }
    
    def report(name: String)(f: => Unit) = BenchmarkReport(name, f _)
}
