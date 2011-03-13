package benchmarks.mongo

import com.mongodb.casbah.Imports._
import java.util.UUID

abstract class DB {
    val mongoConn = MongoConnection()
    val mongoDB = mongoConn("served_benchmarks")
    
    val data = mongoDB("data")
    
    def insert(uuid: UUID) = {
        data += uuid2obj(uuid)
    }
    
    def select(uuid: UUID) = {
        data.findOne(uuid2obj(uuid))
    }
    
    def clear = data.drop
    def close = mongoConn.close
    
    def uuid2obj(uuid: UUID): MongoDBObject
    def setupIndex
}

class DBTuple extends DB {
    def uuid2obj(uuid: UUID) = MongoDBObject("uuid" -> (uuid.getMostSignificantBits, uuid.getLeastSignificantBits))
    def setupIndex = data.ensureIndex("uuid")
}
class DBArray extends DB {
    def uuid2obj(uuid: UUID) = MongoDBObject("uuid" -> Array(uuid.getMostSignificantBits, uuid.getLeastSignificantBits))
    def setupIndex = data.ensureIndex("uuid")
}
class DBList extends DB {
    def uuid2obj(uuid: UUID) = MongoDBObject("uuid" -> List(uuid.getMostSignificantBits, uuid.getLeastSignificantBits))
    def setupIndex = data.ensureIndex("uuid")
}
class DBSeparate extends DB {
    def uuid2obj(uuid: UUID) = MongoDBObject("uuidh" -> uuid.getMostSignificantBits, "uuidl" -> uuid.getLeastSignificantBits)
    def setupIndex = data.ensureIndex(MongoDBObject("uuidh" -> 1, "uuidl" -> 1))
}



object MongoBenchmark {
    def main(args: Array[String]): Unit = {
        val dbs = (new DBList) ::
                  (new DBTuple) ::
                  (new DBArray) ::
                  (new DBSeparate) ::
                  Nil
                  
        val uuids = (1 to 10000) map { i => UUID.randomUUID }
                  
        val runs = (1 to 5) map { i => 
            println("Run #" + i)
            println("=============")
            val d = dbs map { db =>
                println(" === " + db.getClass.getName)
                db.clear
                db.setupIndex

                val ins = bench("  insert"){
                    uuids foreach { id =>
                        db insert id
                    }
                }

                val sel = bench("  select"){
                    uuids foreach { id =>
                        db select id
                    }
                }
                (ins, sel)
            }
            (i,d)
        }
        
        println("insert")
        runs.foreach { case (i,d) =>
            print(i + "\t")
            d foreach { e => 
                printf(e._1 + "\t")
            }
            println()
        }
        
        println("select")
        runs.foreach { case (i,d) =>
            print(i + "\t")
            d foreach { e => 
                printf(e._2 + "\t")
            }
            println()
        }
                  

    }
    
    def bench(title: String)(f: => Unit) = {
        val start = System.currentTimeMillis
        f
        val elapsed = System.currentTimeMillis - start
        printf("%s: %dms\n", title, elapsed)
        elapsed
    }
    
}