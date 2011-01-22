package com.verknowsys.served.utils.monitor

import scala.actors.Actor
import scala.actors.remote.RemoteActor
import scala.actors.remote.RemoteActor._
import scala.actors.remote.Node
import scala.collection.mutable.{ListBuffer, Map}
import scala.swing._

import java.awt.{Font, Color}
// import java.awt.geom.AffineTransform
import javax.swing.table._





    

    
    
    // final val SIZE = new Dimension(1200, 300)
    // final val OFFSET_LEFT = 100
    // final val OFFSET_RIGHT = 10
    // 
    // final val COLOR = Map(
    //     New -> Color.GRAY,
    //     Runnable -> Color.GREEN,
    //     Suspended -> Color.YELLOW,
    //     TimedSuspended -> Color.RED,
    //     Blocked -> Color.ORANGE,
    //     TimedBlocked -> Color.BLUE,
    //     Terminated -> Color.BLACK
    // )
    // 
    // preferredSize = SIZE
    
    // start
    
    // override def paint(g: Graphics2D){        
    //     g.setColor(Color.WHITE)
    //     g.clearRect(0, 0, peer.getWidth, peer.getHeight)
    //     g.setFont(new Font("Monaco", Font.PLAIN, 12))   
    //             
    //     // val maxTime = data.map(_._2.keys.max).max - startTime
    //     
    //     g.setPaint(Color.BLACK)
    //     
    //     runnables.zipWithIndex.foreach { case(name, i) => 
    //         g.drawString(name, 10, 30+i*20)
    //     }
    //     
    //     val fieldWidth = peer.getWidth - OFFSET_LEFT - OFFSET_RIGHT
    //     val n =  fieldWidth / 10
    //     val width = n * 10
    //             
    //     val fontAT = new AffineTransform
    //     fontAT.rotate(Math.toRadians(270))
    //     g.setFont(new Font("Monaco", Font.PLAIN, 8).deriveFont(fontAT))
    //     
    //     data.takeRight(n).zipWithIndex.foreach { case((time, map), t) => 
    //         val x = OFFSET_LEFT + width*t/n
    //         
    //         map.zipWithIndex.foreach { case((name, state), i) => 
    //             g.setPaint(COLOR(state))
    //             g.fillRect(x, 15+i*20, 8, 18)
    //         }
    //            
    //         if(t%5 == 0){
    //             g.setPaint(Color.BLACK)     
    //             g.drawString(time.toString, x+5, 40+map.size*20)
    //         }
    //     }
    //     
    //     g.setFont(new Font("Monaco", Font.PLAIN, 12))   
    //             
    //     COLOR.zipWithIndex.foreach { case((state, color), i) => 
    //         g.setPaint(Color.BLACK)     
    //         g.drawString(state.toString, 10, 60+(i+runnables.size)*20)
    //         g.setPaint(color)
    //         g.fillRect(100, 45+(i+runnables.size)*20, 20, 18)
    //     }
    //     
    //     // data.zipWithIndex.foreach { case ((name, states), i) =>   
    //     //     g.setPaint(Color.BLACK)
    //     //     g.drawString(name, 10, 30+i*20)
    //     //     
    //     //     
    //     //     states.zipWithIndex.foreach { case ((time, state), j) => 
    //     //         g.setPaint(COLOR(state))
    //     //         val x = (time - startTime)/50
    //     //         
    //     //         g.fillRect(OFFSET_LEFT+x.toInt, 15+i*20, 1, 18)
    //     //     }
    //     // }
    //     
    //     
    //     // g.drawString("dupa", 50, 50);
    // }
// }

class Reader(host: String, port: Int, repaintFun: () => Unit) extends Actor { 
    val data = Map[String, List[Actor.State.Value]]()

    start

    def act {
        RemoteActor.classLoader = getClass().getClassLoader()
        val monitor = select(Node(host, port), 'SvdMonitor)
        link(monitor)

        println("Started")

        loop {
            monitor ! GetSvdMonitoredData


            receiveWithin(500) {
                case SvdMonitoredData(time, map) => 
                    map.foreach { case(name, state) => 
                        if(!data.contains(name)) data(name) = List(state)
                        else data(name) = data(name) ++ List(state)
                    }

                    repaintFun()
                    Thread.sleep(10)
                    println("got something...")

                case _ => 
                    println("nothing...")
                    // TODO: Clear (served resterted)
            }
        }
    }

}



object SvdMonitorDisplay extends SimpleSwingApplication {
    import scala.actors.Actor.State._
    
    final val COLORS = Map(
        New -> Color.GRAY,
        Runnable -> Color.GREEN,
        Suspended -> Color.YELLOW,
        TimedSuspended -> Color.RED,
        Blocked -> Color.ORANGE,
        TimedBlocked -> Color.BLUE,
        Terminated -> Color.BLACK
    )
    
    val reader = new Reader("localhost", 8888, update _)
    
    val table = new SvdSplitTable("ID", "State", reader.data, (g: Graphics2D, data: List[Actor.State.Value]) => {
        data.zipWithIndex.foreach { case (state, i) =>
           g.setPaint(COLORS(state))
           g.fillRect(i*10, 0, 8, 20)
        }
        
        data.size * 10
    })
    
    def update { table.repaint }
    
    def top = new MainFrame {
        title = "ServeD Actors SvdMonitor"
        location = new Point(100, 100)
        contents = table
    }
}







// 
// import scala.collection.mutable.Map
// import swing._
// import javax.swing.table.AbstractTableModel
// import java.io._
// 
// object SvdMonitorDisplay extends SimpleSwingApplication {
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
//         title = "Actors SvdMonitor Display"
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