package com.verknowsys.served.utils.monitor

import scala.actors.Actor
import scala.actors.remote.RemoteActor
import scala.actors.remote.RemoteActor._
import scala.actors.remote.Node
import scala.collection.mutable.ListBuffer
import scala.swing._

import java.awt.{Font, Color}
import java.awt.geom.AffineTransform
// import javax.swing._ 

class Graph(host: String, port: Int) extends Panel with Actor {  
    import Actor.State._
    
    val data = new ListBuffer[(Long, Map[String, Actor.State.Value])] // List[(time, Map[name, state])]
    val runnables = new ListBuffer[String]
    
    // val data = Map[String, Map[Long, Actor.State.Value]]() // Map[name, Map[time, state]]
    // var startTime = 0L

    def act {
        RemoteActor.classLoader = getClass().getClassLoader()
        val monitor = select(Node(host, port), 'SvdMonitor)
        link(monitor)
        
        println("Started")
        
        loop {
            monitor ! GetMonitoredData
            
            
            receiveWithin(500) {
                case MonitoredData(time, map) => 
                    data += ((time, map))  
                    map foreach { case(name, state) => if(!runnables.contains(name)) runnables += name }
                
                    repaint
                    Thread.sleep(10)
                
                case _ => 
                    println("WTF?")
                    // startTime = 0
                    // TODO: Clear (served resterted)
            }
        }

    }
    
    final val SIZE = new Dimension(1200, 300)
    final val OFFSET_LEFT = 100
    final val OFFSET_RIGHT = 10
    
    final val COLOR = Map(
        New -> Color.GRAY,
        Runnable -> Color.GREEN,
        Suspended -> Color.YELLOW,
        TimedSuspended -> Color.RED,
        Blocked -> Color.ORANGE,
        TimedBlocked -> Color.BLUE,
        Terminated -> Color.BLACK
    )
    
    preferredSize = SIZE
    
    start
    
    override def paint(g: Graphics2D){
        val metrics = g.getFontMetrics
        
        g.setColor(Color.WHITE)
        g.clearRect(0, 0, peer.getWidth, peer.getHeight)
        g.setFont(new Font("Monaco", Font.PLAIN, 12))   
                
        // val maxTime = data.map(_._2.keys.max).max - startTime
        
        g.setPaint(Color.BLACK)
        
        runnables.zipWithIndex.foreach { case(name, i) => 
            g.drawString(name, 10, 30+i*20)
        }
        
        val fieldWidth = peer.getWidth - OFFSET_LEFT - OFFSET_RIGHT
        val n =  fieldWidth / 10
        val width = n * 10
        
        val fontAT = new AffineTransform
        fontAT.rotate(Math.toRadians(270))
        g.setFont(new Font("Monaco", Font.PLAIN, 8).deriveFont(fontAT))
        
        data.takeRight(n).zipWithIndex.foreach { case((time, map), t) => 
            val x = OFFSET_LEFT + width*t/n
            
            map.zipWithIndex.foreach { case((name, state), i) => 
                g.setPaint(COLOR(state))
                g.fillRect(x, 15+i*20, 8, 18)
            }
               
            if(t%5 == 0){
                g.setPaint(Color.BLACK)     
                g.drawString(time.toString, x+5, 40+map.size*20)
            }
        }
        
        g.setFont(new Font("Monaco", Font.PLAIN, 12))   
                
        COLOR.zipWithIndex.foreach { case((state, color), i) => 
            g.setPaint(Color.BLACK)     
            g.drawString(state.toString, 10, 60+(i+runnables.size)*20)
            g.setPaint(color)
            g.fillRect(100, 45+(i+runnables.size)*20, 20, 18)
        }
        
        // data.zipWithIndex.foreach { case ((name, states), i) =>   
        //     g.setPaint(Color.BLACK)
        //     g.drawString(name, 10, 30+i*20)
        //     
        //     
        //     states.zipWithIndex.foreach { case ((time, state), j) => 
        //         g.setPaint(COLOR(state))
        //         val x = (time - startTime)/50
        //         
        //         g.fillRect(OFFSET_LEFT+x.toInt, 15+i*20, 1, 18)
        //     }
        // }
        
        
        // g.drawString("dupa", 50, 50);
    }
}

object MonitorDisplay extends SimpleSwingApplication {    
    val graph = new Graph("localhost", 8888)
    
    def top = new MainFrame {
        title = "ServeD Actors Monitor"
        contents = graph
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