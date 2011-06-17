package com.verknowsys.served.db

import com.verknowsys.served.utils.Benchmark._
import com.verknowsys.served.SvdSpecHelpers._
import com.verknowsys.served.utils._
import com.verknowsys.served._

import org.neodatis.odb._
import org.neodatis.odb.impl.core.query.criteria.CriteriaQuery
import org.neodatis.odb.core.query.criteria.Where
import org.neodatis.odb.core.query.nq.SimpleNativeQuery


case class Foo(val uuid: UUID = randomUUID)


object QueryBenchmark {
    def main(args: Array[String]): Unit = {
        val dbname = SvdConfig.systemTmpDir / "svd_bench/svd_db_query_benchmark.neodatis"
        rmdir(SvdConfig.systemTmpDir / "svd_bench")
        
        val server = ODBFactory.openServer(randomPort)

        server.addBase("base", dbname)
        server.startServer(true)
        
        
        
        
        println("Storing objects")
        
        var odb = server.openClient("base")
        
        val objects = (1 to 10000) map { i => new Foo }
    
        (1 to 100) foreach { i =>
            objects foreach { o => odb.store(o) }
        }
        
        odb.close
        odb = server.openClient("base")
        
        val first = objects.head
        
        
        
        
        println("Testing results")
        
        println(odb.getObjects(new SimpleNativeQuery {
            def `match`(obj: Foo): Boolean = {
                obj.uuid.compareTo(first.uuid) == 0
            }
        }))
        
        println(odb.getObjects(new SimpleNativeQuery {
            def `match`(obj: Foo): Boolean = {
                obj.uuid.getMostSignificantBits == first.uuid.getMostSignificantBits &&
                    obj.uuid.getLeastSignificantBits == first.uuid.getLeastSignificantBits
            }
        }))
        

        println(odb.getObjects(new CriteriaQuery(classOf[Foo], 
            Where.and()
                .add(Where.equal("uuid.mostSigBits", first.uuid.getMostSignificantBits))
                .add(Where.equal("uuid.leastSigBits", first.uuid.getLeastSignificantBits))
        )))
        
        
        
        
        println("Benchmarking query")
        
        odb.close
        odb = server.openClient("base")
        
        val nq1 = new SimpleNativeQuery {
            def `match`(obj: Foo): Boolean = {
                obj.uuid.compareTo(first.uuid) == 0
            }
        }
        
        val nq2 = new SimpleNativeQuery {
            def `match`(obj: Foo): Boolean = {
                obj.uuid.getMostSignificantBits == first.uuid.getMostSignificantBits &&
                    obj.uuid.getLeastSignificantBits == first.uuid.getLeastSignificantBits
            }
        }
        
        val cq = new CriteriaQuery(classOf[Foo], 
            Where.and()
                .add(Where.equal("uuid.mostSigBits", first.uuid.getMostSignificantBits))
                .add(Where.equal("uuid.leastSigBits", first.uuid.getLeastSignificantBits))
        )

        
        benchmark(100){
            report("native query #1"){ 
                odb.getObjects(nq1)
                ()
            } ::
            report("native query #2"){ 
                odb.getObjects(nq2)
                ()
            } ::
            report("criteria query"){
                odb.getObjects(cq)
                ()
            } ::
            Nil
        }
        
        odb.close
    }
}