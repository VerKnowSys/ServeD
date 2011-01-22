package com.verknowsys.served.utils

import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.pattern.ClassicConverter
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.Level._


/** 
 * Colorful message in console. Used by logback logger
 * 
 * See svd.conf/src/main/resources/logback.xml for loggerconfiguration
 * 
 * @author teamon
 */
class ANSIColorPatternLayout extends PatternLayout {
    PatternLayout.defaultConverterMap.put("level", classOf[LowerCaseLevelConverter].getName);
    
    final val Colors = Map(
        TRACE -> Console.MAGENTA,
        DEBUG -> Console.CYAN,
        INFO  -> Console.WHITE,
        WARN  -> Console.YELLOW,
        ERROR -> Console.RED
    )

    final val DefaultColor = Console.RESET
    
    override def doLayout(event: ILoggingEvent) = Colors(event.getLevel) + super.doLayout(event) + DefaultColor
    
}

class LowerCaseLevelConverter extends ClassicConverter {
    def convert(le: ILoggingEvent) = le.getLevel.toString.toLowerCase
}
