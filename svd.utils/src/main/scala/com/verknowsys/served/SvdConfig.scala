package com.verknowsys.served

import scala.io.Source

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

    def version                         = "0.2.0" + "-" + Source.fromURL(getClass.getResource("/VERSION")).getLines.mkString
    def copyright                       = "Copyright © 2oo9-2o11 VerKnowSys.com - All Rights Reserved."
    def kickApp                         = "svdkick"
    def servedShell                     = "svdshell"
    def rootPidFile                     = "0.pid"
    def coreSvdDatabaseName             = "svd.db"
    def coreSvdAccountsDatabaseName     = "accounts.db"
    def served                          = "ServeD v" + version
    def systemConfDir                   = "/etc"
    def systemHomeDir                   = "/SystemUsers/"
    def userHomeDir                     = "/Users/"
    def softwareRoot                    = "/Software/"
    def applicationsDir                 = "Apps/"
    def installed                       = "INSTALLED"
    def defaultShell                    = "/bin/sh"
    def defaultEncoding                 = "UTF-8"
    def publicHttpDir                   = "/Public/"
    def defaultHost                     = "127.0.0.1"
    def defaultDomain                   = "localhost"
    def remoteApiServerHost             = "127.0.0.1" // 2011-06-09 00:15:00 - dmilith - TODO: XXX: switch to automatic ip detection (one with default route set)
    def sshPort                         = 22
    def remoteApiServerPort             = 10
    def remoteAccountServerPort         = 12

    def defaultHttpKeepAliveTimeout     = 65
    def defaultHttpAmountOfWorkers      = 2
    def defaultHttpWorkerConnections    = 1024
    def defaultHttpPort                 = 80 /* NOTE: also make sure that this port is automatically registered to be occupied by http server */
    def defaultHttpsPort                = 443 /* NOTE: also make sure that this port is automatically registered to be occupied by http server */

    def defaultUserGroup                = 0 // from now, every file created for user in filesystem will have 0 uid
    def kqueueWaitInterval              = 500
    def sleepDefaultPause               = 1000
    def gatherTimeout                   = 1000
    def standardShellEnvironment        = ". /etc/profile\nulimit -u 120\n"
    def notificationXmppRecipients      = "dmilith@verknowsys.com,i@teamon.eu"
    def notificationXmppHost            = "verknowsys.com"
    def notificationXmppPort            = 5222
    def notificationXmppLogin           = "svd-bot"
    def notificationXmppPassword        = "svd-bot-no-passwd"
    def notificationXmppResource        = "svd"
    def notificationXmppDebug           = false
    def notificationXmppCompression     = true
    def notificationXmppUseSasl         = true


}
