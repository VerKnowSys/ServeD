package com.verknowsys.served.web.lib

object Helpers {
    def linkTo(label: String, path: String) = <a href={path}>{label}</a>
}
