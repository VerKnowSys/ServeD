package com.verknowsys.served.utils

import scala.collection.mutable.ListBuffer

object SvdBenchmark {
    class SvdBenchmark(n: Int){
        val tests = new ListBuffer[(String, () => Unit)]
        
        def apply(name: String)(func: => Unit){
            tests += ((name, func _))
        }
        
        def run {
            tests.foreach { case (name, func) =>
                val start = System.currentTimeMillis
        
                (1 to n) foreach { i => func() }
                val time = System.currentTimeMillis - start
                
                println(name + "   " + time)
            }
        }
    }
    
    def apply(n: Int)(f: (SvdBenchmark) => Unit){
        val bench = new SvdBenchmark(n)
        f(bench)
        bench.run
    }
}