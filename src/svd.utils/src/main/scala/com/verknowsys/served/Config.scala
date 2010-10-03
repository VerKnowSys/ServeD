package com.verknowsys.served

import com.verknowsys.served.utils.Properties


object Config {


    /**
    * @author dmilith
    *
    * A very low level settings hardcoded into application
    *
    */

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

    val etcPath = props("etcPath") getOrElse defaultS("etcPath", "/etc/")
    val passwdFileName = props("passwdFileName") getOrElse defaultS("passwdFileName", "posswd")
    val checkInterval = props.int("checkInterval") getOrElse defaultI("checkInterval", 1500)
    val sizeMultiplier = props.int("sizeMultiplier") getOrElse defaultI("sizeMultiplier", 1024)
    val defaultGitRepoToWatch = props("defaultGitRepoToWatch") getOrElse defaultS("defaultGitRepoToWatch", "/git/ServeD.git")

    val xmppHost = props("xmpp.host") getOrElse defaultS("xmpp.host", "localhost")
    val xmppPort = props.int("xmpp.port") getOrElse defaultI("xmpp.port", 5222)
    val xmppLogin = props("xmpp.login") getOrElse defaultS("xmpp.login", "gitbot")
    val xmppPassword = props("xmpp.password") getOrElse defaultS("xmpp.password", "git-bot-666")
    val xmppResource = props("xmpp.resource") getOrElse defaultS("xmpp.resource", "served-bot-resource")

    /**
    *   @author dmilith
    *   Shortcuts for setting default values and storing them in config
    */
    def defaultS(key: String, value: String) = {
        props(key) = value
        value
    }

    def defaultI(key: String, value: Int) = {
        props(key) = value
        value
    }

}
