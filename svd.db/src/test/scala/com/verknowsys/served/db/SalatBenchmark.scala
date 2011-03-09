package com.verknowsys.served.db

import com.novus.salat._
import com.novus.salat.global._
import com.mongodb.casbah.Imports._

object SalatBenchmark {
    import com.verknowsys.served.utils.Benchmark._
    
    def main(args: Array[String]) {
        val db = new DB
        
        val uuid = java.util.UUID.randomUUID
        
        (1 to 5) foreach { i =>
            benchmark(10000)(
                report("insert (salat)"){
                    db.current.insert(grater[Config].asDBObject(Config(uuid, "teamon")))
                } ::
                report("insert (cashbah)"){ 
                    db.current.insert(MongoDBObject("uuid" -> uuid, "name" -> "teamon"))
                } ::
                Nil
            )
        }
    }   
}
