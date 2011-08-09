package org.slf4j.impl

import org.slf4j.Logger
import org.slf4j.ILoggerFactory

object SvdSFL4JLoggerFactory {
    final val INSTANCE = new SvdSFL4JLoggerFactory
}

class SvdSFL4JLoggerFactory extends ILoggerFactory {

    val loggerMap = scala.collection.mutable.Map[String, Logger]()

    def getLogger(name: String): Logger = synchronized {
        loggerMap.get(name) getOrElse {
            val slogger = new SvdSLF4JLogger(name)
            loggerMap(name) = slogger
            slogger
        }
    }
}