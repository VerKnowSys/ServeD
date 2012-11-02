package com.verknowsys.served


package object services {

    implicit def nameToSvdServiceConfiguration(name: String) = new SvdServiceConfigLoader(name).config

    implicit def shellOperationsAsString(commands: String) = List(commands)


}
