// package com.verknowsys.served.utils.monitor
// 
// import scala.swing._
// import java.awt.{Font, Color}
// import javax.swing.table._
// 
// class SvdSplitTable[A, B](keyName: String, valueName: String, data: scala.collection.Map[A, B], draw: (Graphics2D, B) => Int) extends BoxPanel(Orientation.Horizontal) {
//     val keyTable = new Table {
//         model = new AbstractTableModel {
//             override def getColumnName(col: Int) = keyName
//             def getRowCount = data.size
//             def getColumnCount = 1
//             def getValueAt(row: Int, col: Int) = data.toList(row)._1.asInstanceOf[Object]
//         }
//     }
//     
//     val valueTable = new Table  {
//         autoResizeMode = Table.AutoResizeMode.Off
//         model = new AbstractTableModel {
//             override def getColumnName(col: Int) = valueName
//             def getRowCount = data.size
//             def getColumnCount = 1
//             def getValueAt(row: Int, col: Int) = data.toList(row)._2.asInstanceOf[Object]
//         }
//         
//         def currentWidth = peer.getColumnModel.getColumn(0).getPreferredWidth()
//         def currentWidth_=(w: Int) = peer.getColumnModel.getColumn(0).setPreferredWidth(w)
//         
//         override protected def rendererComponent(isSelected: Boolean, focused: Boolean, row: Int, column: Int): Component = {
//             apply(row, column) match {
//                 case xs:B => new Panel {
//                     override def paint(g: Graphics2D){
//                        val width = draw(g, xs)
//                        if(currentWidth < width) currentWidth = width
//                        autoscroll(width)
//                     }
//                 }
//                 case _ => new Label("???")
//             }
//         }
// 
//     }
//     
//     val keyScroll = new ScrollPane(keyTable){
//         preferredSize = new Dimension(200, 300)
//     }
//     val valueScroll = new ScrollPane(valueTable){
//         preferredSize = new Dimension(500, 300)
//     }
//     
//     def autoscroll(width: Int) {
//         val scroll = valueScroll.horizontalScrollBar.peer
//         if(!scroll.getValueIsAdjusting()) scroll.setValue(width)
//     }
//     
//     keyScroll.horizontalScrollBarPolicy = ScrollPane.BarPolicy.AsNeeded
//     valueScroll.horizontalScrollBarPolicy = ScrollPane.BarPolicy.AsNeeded
//     
//     contents += keyScroll
//     contents += valueScroll
// }