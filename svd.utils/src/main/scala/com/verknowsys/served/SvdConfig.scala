package com.verknowsys.served

import com.verknowsys.served.utils.SvdProperties


object SvdConfig {


    /**
     * @author dmilith, teamon
     *
     * A very low level settings hardcoded into application
     *
     */

    final val vendorDir = ".svd/"
    final val mainPropertiesFilename = "svd.properties"
    final val homePath = System.getProperty("user.home") + "/"
    final val mainSvdConfigFile = homePath + vendorDir + mainPropertiesFilename
    final val props = new SvdProperties(mainSvdConfigFile)

    final val env = Array(
        "TERM=%s".format(terminalType),
        "TMPDIR=%s".format(defaultTmpDir),
        "LC_CTYPE=%s".format(defaultEncoding),
        "PWD=%s".format(homePath + vendorDir),
        "COMMAND_MODE=%s".format(terminalCommandMode),
        "HOME=%s".format(homePath),
        "PATH=%s".format(defaultPathEnviroment) // 2011-01-18 14:39:36 - dmilith - TODO: implement account privileges and their influence on PATH setting
    )


    /**
     *   @author dmilith, teamon
     *   More dynamic settings from main properties file
     */

     
    def defaultEncoding =               props("served.system.encoding") or "UTF-8"
    def defaultPathEnviroment =         props("served.system.terminal.environment.path") or "$HOME/bin:$HOME/Bin:/bin:/usr/bin:/usr/local/bin"
    def defaultTmpDir =                 props("served.system.terminal.environment.tmpdir") or "/tmp/"
    def terminalType =                  props("served.system.terminal.environment.term") or "xterm-color"
    def terminalCommandMode =           props("served.system.terminal.environment.mode") or "unix2003"
    def servedUserName =                props("served.system.username") or "served"
    def systemPasswdFile =              props("served.system.password.filename") or homePath + vendorDir + "etc/passwd"
    
    def notificationXmppDebug =        props("served.notification.xmpp0.debug") or false
    def notificationXmppCompression =  props("served.notification.xmpp0.compression") or true
    def notificationXmppUseSasl =      props("served.notification.xmpp0.usesasl") or true
    def notificationXmppRecipients =   props("served.notification.xmpp0.recipients") or "dmilith@verknowsys.com,i@teamon.eu"
    def notificationXmppHost =         props("served.notification.xmpp0.host") or "127.0.0.1"
    def notificationXmppPort =         props("served.notification.xmpp0.port") or 5222
    def notificationXmppLogin =        props("served.notification.xmpp0.login") or "gitbot"
    def notificationXmppPassword =     props("served.notification.xmpp0.password") or "git-bot-666"
    def notificationXmppResource =     props("served.notification.xmpp0.resource") or "served-bot-notifier"
    
    def remoteApiServerHost =          props("served.remote.apiserver.host") or "localhost"
    def remoteApiServerPort =          props("served.remote.apiserver.port") or 5555
    
    
    def apply(key: String) = props(key)
    

}
