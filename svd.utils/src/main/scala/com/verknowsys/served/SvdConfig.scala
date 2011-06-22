package com.verknowsys.served

import com.verknowsys.served.utils._
import com.verknowsys.served.utils.SvdProperties

object SvdConfig {

    /**
     * @author dmilith, teamon
     *
     * A very low level settings hardcoded into application
     * These configuration settings can be overriden by configuration stored in ServeD database
     *
     */
     
    final val version = "0.2.0"
    final val served = "ServeD v" + version
    final val systemConfDir = "/etc"
    final val systemLogDir = "/var/log"
    final val systemTmpDir = "/var/tmp"
    final val systemHomeDir = "/SystemUsers/"
    final val userHomeDir = "/Users/"
    // final val systemPasswdFile = systemConfDir / "passwd"
    final val defaultEncoding = "UTF-8"
    final val remoteApiServerHost = "localhost" // 2011-06-09 00:15:00 - dmilith - TODO: XXX: switch to automatic ip detection (one with default route set)
    final val remoteApiServerPort = 10
    final val defaultUserGroup = 20 // 2011-06-09 01:06:59 - dmilith - NOTE: default GID for "staff" group both on Darwin and FreeBSD
    
    val kqueueWaitInterval = 500
    val sleepDefaultPause = 1000
    val gatherTimeout = 1000

    val notificationXmppRecipients = "dmilith@verknowsys.com,i@teamon.eu"
    val notificationXmppHost = "verknowsys.com"
    val notificationXmppPort = 5222
    val notificationXmppLogin = "svd-bot"
    val notificationXmppPassword = "svd-bot-no-passwd"
    val notificationXmppResource = "svd"
    val notificationXmppDebug = false
    val notificationXmppCompression = true
    val notificationXmppUseSasl = true
    
}
