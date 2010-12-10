package com.verknowsys.served.utils.monitor

import scala.actors.Actor
import scala.actors.remote.RemoteActor
import scala.actors.remote.RemoteActor._
import scala.actors.remote.Node

class Reader(peer: Node) extends Actor {    
    def act {
        RemoteActor.classLoader = getClass().getClassLoader()
        val monitor = select(peer, 'SvdMonitor)
        link(monitor)
        
        println("Started")
        
        loop {
            monitor ! GetMonitoredData
            
            
            receive {
                case MonitoredData(time, list) => 
                    println(time)
                    println(list)
                
                case _ => println("WTF?")
            }
        }

    }
}

object MonitorDisplay {
    def main(args: Array[String]): Unit = {
        // if(args.length == 2) 
        val peer = Node("localhost", 8888)
        val reader = new Reader(peer)
        reader.start
        // else { println("Usage: com.verknowsys.served.utils.monitor.MonitorDisplay HOST PORT") }
    }
}







// 
// import scala.collection.mutable.Map
// import swing._
// import javax.swing.table.AbstractTableModel
// import java.io._
// 
// object MonitorDisplay extends SimpleSwingApplication {
//     class Model extends AbstractTableModel {
//         val data = Map[String, Array[String]]()
//         
//         val columnNames = List("ID", "Class", "State")
//         
//         override def getColumnName(col: Int) = columnNames(col)
//         
//         def getRowCount = data.size
//         
//         def getColumnCount = columnNames.length
//         
//         def getValueAt(row: Int, col: Int) = col match {
//             case 0 => data.toList(row)._1
//             case i:Int => data.toList(row)._2(i-1)
//             case _ => ""
//         }
//     }
//     
//     val table = new Table
//     val model = new Model
//     table.model = model
//     
//     table.peer.getColumnModel.getColumn(0).setPreferredWidth(100)
//     table.peer.getColumnModel.getColumn(1).setPreferredWidth(500)
//     table.peer.getColumnModel.getColumn(2).setPreferredWidth(100)
//     
//     def top = new MainFrame {
//         title = "Actors Monitor Display"
//         location = new Point(100, 100)
//         
//         contents = new ScrollPane {
//             preferredSize = new Dimension(600, 300)
//             contents = table
//         }   
//     }
//     
//     override def main(args: Array[String]){
//         super.main(args)
//         
//         val br = new BufferedReader(new InputStreamReader(System.in))
//                 
//         while(true){
//             val chunks = br.readLine.split(",")
//             if(chunks.length == 3){
//                 model.data(chunks.head) = chunks.tail
//                 model.fireTableDataChanged
//             }
//         }
//     }
// } 