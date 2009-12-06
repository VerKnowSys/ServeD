package served

import utils.Utils
import org.apache.log4j.Logger

/**
 * User: dmilith
 * Date: Dec 6, 2009
 * Time: 2:51:47 AM
 */


object ServeD extends Utils {

	override def logger: Logger = Logger.getLogger(ServeD.getClass)

	/**
	 * Main ServeD loader.
	 */
	def main(args: Array[String]) {

		addShutdownHook {
			logger.info("ServeD has ended")
		}
		
		initLogger
		logger.info("ServeD is loading")

	}



}