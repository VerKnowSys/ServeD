package com.verknowsys.served.utils

import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.Level._


class ANSIColorPatternLayout extends PatternLayout {
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