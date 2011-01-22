package com.verknowsys.served.maintainer

import java.net.URL
import scala.xml.XML

object SvdCrawler {
    def main(args: Array[String]): Unit = {
        println(XML.load(new URL("http://teamon.eu")))
                
    }
}
