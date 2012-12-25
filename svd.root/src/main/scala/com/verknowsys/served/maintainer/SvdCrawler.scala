/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.maintainer

import java.net.URL
import scala.xml.XML

object SvdCrawler {
    def main(args: Array[String]): Unit = {
        println(XML.load(new URL("http://teamon.eu")))
                
    }
}
