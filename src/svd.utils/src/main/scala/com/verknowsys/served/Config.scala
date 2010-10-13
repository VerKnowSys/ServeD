package com.verknowsys.served

import com.verknowsys.served.utils.Properties


object Config {


    /**
    * @author dmilith
    *
    * A very low level settings hardcoded into application
    *
    */

    final val etcPath = "/etc/"
    final val passwdFileName = "passwd"
    final val vendorDir = ".svd/"
    final val mainPropertiesFile = "svd.properties"
    final val loggerPropertiesFile = "logger.properties"
    final val homePath = System.getProperty("user.home") + "/"
    final val systemPasswdFile = etcPath + passwdFileName
    final val mainConfigFile = homePath + vendorDir + mainPropertiesFile
    final val mainLoggerFile = homePath + vendorDir + loggerPropertiesFile
    final val props = new Properties(mainConfigFile)
    

    /**
    *   @author dmilith
    *   More dynamic settings from main properties file
    */

    val checkInterval = props.int("checkInterval", 1500)
    val sizeMultiplier = props.int("sizeMultiplier", 1024)
    val defaultGitRepoToWatch = props("defaultGitRepoToWatch", "/git/ServeD.git")

    val xmppHost = props("xmpp.host", "localhost")
    val xmppPort = props.int("xmpp.port", 5222)
    val xmppLogin = props("xmpp.login", "gitbot")
    val xmppPassword = props("xmpp.password", "git-bot-666")
    val xmppResource = props("xmpp.resource", "served-bot-resource")

}
