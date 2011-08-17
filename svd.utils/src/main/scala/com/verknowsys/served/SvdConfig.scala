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

    def version                     = "0.2.0"
    def kickApp                     = "svdkick"
    def servedShell                 = "svdshell"
    def rootPidFile                 = "0.pid"
    def coreSvdDatabaseName         = "svd.db"
    def coreSvdAccountsDatabaseName = "accounts.db"
    def served                      = "ServeD v" + version
    def systemConfDir               = "/etc"
    def systemHomeDir               = "/SystemUsers/"
    def userHomeDir                 = "/Users/"
    def defaultShell                = "/bin/sh"
    def defaultEncoding             = "UTF-8"
    def defaultHost                 = "127.0.0.1"
    def remoteApiServerHost         = "127.0.0.1" // 2011-06-09 00:15:00 - dmilith - TODO: XXX: switch to automatic ip detection (one with default route set)
    def sshPort                     = 22
    def remoteApiServerPort         = 10
    def remoteAccountServerPort     = 12
    def defaultUserGroup            = 0 // from now, every file created for user in filesystem will have 0 uid
    def kqueueWaitInterval          = 500
    def sleepDefaultPause           = 1000
    def gatherTimeout               = 1000
    def standardShellEnvironment    = ". /etc/profile\nulimit -u 120\n"
    def notificationXmppRecipients  = "dmilith@verknowsys.com,i@teamon.eu"
    def notificationXmppHost        = "verknowsys.com"
    def notificationXmppPort        = 5222
    def notificationXmppLogin       = "svd-bot"
    def notificationXmppPassword    = "svd-bot-no-passwd"
    def notificationXmppResource    = "svd"
    def notificationXmppDebug       = false
    def notificationXmppCompression = true
    def notificationXmppUseSasl     = true


}
