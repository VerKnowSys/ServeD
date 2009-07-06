// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package deployer

import java.awt.{Dimension, Rectangle}
import swing.{MainFrame, Frame, SimpleGUIApplication}

/**
 * User: dmilith
 * Date: Jul 6, 2009
 * Time: 1:33:54 AM
 */


object DeployerGUI extends SimpleGUIApplication {

	def top = new MainFrame {
		title = "Deployer GUI"
		size = new Dimension(400,600)
	}

}