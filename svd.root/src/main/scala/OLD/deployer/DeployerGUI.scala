// 
// // © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// // This Software is a close code project. You may not redistribute this code without permission of author.
// 
// package deployer
// 
// import org.apache.log4j.{ConsoleAppender, Level, PatternLayout, Logger}
// import props.Preferences
// import utils.{SvdUtils}
// import com.trolltech.qt.core._
// import com.trolltech.qt.core.Qt.WindowStates
// import actors.Actor;
// import com.trolltech.qt.gui._;
// import com.trolltech.qt.webkit._;
// 
// /**
//  * User: dmilith
//  * Date: Jul 6, 2009
//  * Time: 1:33:54 AM
//  */
// 
// 
// class GUI extends QWidget {
// 
//  val WIDTH = 250
//     val HEIGHT = 150
// 
// // val qdw = new QDesktopWidget
// // val screenWidth = qdw.width
// // val screenHeight = qdw.height
//  
//  override def paintEvent(event: QPaintEvent) {
//      val painter = new QPainter(this)
//      drawPatterns(painter)
//  }
// 
//  def drawPatterns(painter: QPainter) {
// 
//        painter.setPen(QPen.NoPen)
// 
//        painter.setBrush(Qt.BrushStyle.HorPattern)
//        painter.drawRect(10, 15, 90, 60)
// 
//        painter.setBrush(Qt.BrushStyle.VerPattern)
//        painter.drawRect(130, 15, 90, 60)
// 
//        painter.setBrush(Qt.BrushStyle.CrossPattern)
//        painter.drawRect(250, 15, 90, 60)
// 
//        painter.setBrush(Qt.BrushStyle.Dense7Pattern)
//        painter.drawRect(10, 105, 90, 60)
// 
//        painter.setBrush(Qt.BrushStyle.Dense6Pattern)
//        painter.drawRect(130, 105, 90, 60)
// 
//        painter.setBrush(Qt.BrushStyle.Dense5Pattern)
//        painter.drawRect(250, 105, 90, 60)
// 
//        painter.setBrush(Qt.BrushStyle.BDiagPattern)
//        painter.drawRect(10, 195, 90, 60)
// 
//        painter.setBrush(Qt.BrushStyle.FDiagPattern)
//        painter.drawRect(130, 195, 90, 60)
// 
//        painter.setBrush(Qt.BrushStyle.DiagCrossPattern)
//        painter.drawRect(250, 195, 90, 60)
// 
//        painter.end
//  }
// 
// 
// // override def act = {
// //   react {
// //     case _ =>
// //       act
// //   }
// // }
//  val zz = new QLabel
//  zz.setText("Oto label co się zmieniać ma")
//  zz.setParent(this)
//  resize(WIDTH*2, HEIGHT*2)
//  move(100, 100)
//  //setAnimated(true)
//  setWindowTitle("DeployerGUI")
//  zz.move(10,100)
//  Thread.sleep(1500)
//  zz.move(10,10)
//  show
//  raise
// 
// }
// 
// 
// object DeployerGUI extends SvdUtilsCommon {
// 
//  override
//  def logger = Logger.getLogger(DeployerGUI.getClass)
//  initLogger
// 
//  var props: Preferences = _
// 
// 
//  def main(args: Array[String]) {
//    info("Starting DeployerGUI with ")
// 
//    QApplication.initialize(args)
// 
//    debug("Initialized")
//    val gui = new GUI
// //   gui.start
// //   gui ! 0
//    debug("Created Window")
// 
//    props = new Preferences(args(0))
//    if (props("debug")) {
//      setLoggerLevelDebug(Level.TRACE)
//    }
//    debug("Finished")
// 
//    QApplication.exec
//  }
// 
// 
// 
// // def top = new MainFrame {
// //   addShutdownHook {
// //     warn("Done")
// //   }
// //   title = "Deployer GUI"
// //   size = (600,750)
// //   location = new Point(300, 50)
// //   prepare
// // }
// 
// // def prepare = {
// //   autoDetectRequirements(props)
// //   warn(props("gitExecutable"))
// //   warn(props("jarSignerExecutable"))
// // }
// 
// 
// 
// }
