package com.verknowsys.served.web.filters

import com.verknowsys.served.api.Logger
import com.verknowsys.served.utils.LoggerUtils

class Main extends LoggerFilter {
    LoggerUtils.addEntry("org.fusesource", Logger.Levels.Info)
    LoggerUtils.addEntry("org.scalatra", Logger.Levels.Info)

    println(LoggerUtils.levels)
}
