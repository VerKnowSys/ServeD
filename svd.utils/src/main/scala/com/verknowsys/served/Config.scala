package com.verknowsys.served

import com.verknowsys.served.utils.Properties


object Config {


    /**
     * @author dmilith, teamon
     *
     * A very low level settings hardcoded into application
     *
     */

    final val vendorDir = ".svd/"
    final val mainPropertiesFilename = "svd.properties"
    final val loggerPropertiesFilename = "logger.properties"
    
    final val homePath = System.getProperty("user.home") + "/"
    final val mainConfigFile = homePath + vendorDir + mainPropertiesFilename
    final val loggerConfigFile = homePath + vendorDir + loggerPropertiesFilename

    final val props = new Properties(mainConfigFile)
    

    /**
     *   @author dmilith, teamon
     *   More dynamic settings from main properties file
     */

    def systemPasswdFile = props.get("systemPasswdFile") or homePath + vendorDir + "etc/passwd"
    def checkInterval = props.get("checkInterval") or 1500
    def sizeMultiplier = props.get("sizeMultiplier") or 1024

    def defaultGitRepoToWatch = props.get("defaultGitRepoToWatch") or "/git/ServeD.git" // XXX: Remove this
}
