package com.verknowsys.served.web.filters

import com.verknowsys.served.web.lib._
import com.verknowsys.served.api.Logger._

class LoggerFilter extends REST {
    def prefix = "logger"

    def index = {
        Map(
            "entries" -> (ListEntries <> { case Entries(entries) => entries } getOrElse Map())
        )

    }
}
