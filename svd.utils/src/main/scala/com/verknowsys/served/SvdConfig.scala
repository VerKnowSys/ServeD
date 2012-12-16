package com.verknowsys.served

import scala.io.Source

import com.verknowsys.served._
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

    def version                         = Source.fromURL(getClass.getResource("/FULLVERSION")).getLines.mkString + "-b" + Source.fromURL(getClass.getResource("/BUILD")).getLines.mkString
    def copyright                       = "Copyright © 2oo9-2o12 VerKnowSys.com - All Rights Reserved."
    def operatingSystem                 = System.getProperty("os.name")
    def binaryArchitecture              = "64"
    def systemVersion                   = System.getProperty("os.version")
    def binarySoftwareRepository        = "http://software.verknowsys.com/binary" / operatingSystem / binaryArchitecture / systemVersion
    def kickApp                         = "svdkick"
    def servedShell                     = "svdshell"
    def rootPidFile                     = "0.pid"
    def coreSvdDatabaseName             = "svd.db"
    def coreSvdAccountsDatabaseName     = "accounts.db"
    def served                          = "ServeD"
    def servedFull                      = "%s v%s".format(served, version)
    def systemConfDir                   = "/etc/"
    def temporaryDir                    = "/tmp/"
    def systemHomeDir                   = "/SystemUsers/"
    def userHomeDir                     = "/Users/"
    def softwareRoot                    = "/Software/"
    def applicationsDir                 = "Apps/"
    def webApplicationsDir              = "WebApps/"
    def webConfigDir                    = "WebConfig/"
    def softwareDataDir                 = "SoftwareData/"
    def defaultServicesFile             = "services.defined"
    def defaultServiceLogFile           = "service.log"
    def installed                       = "installed"
    def defaultShell                    = "/Software/Zsh/exports/zsh"
    def defaultSchedulerShellTimeout    = 60000
    def defaultEncoding                 = "UTF-8"
    def publicHttpDir                   = "/Public/"
    def defaultBackupDir                = "/Backup/"
    def backupKey                       = "30E4A7AF-0624-4288-8CD6-31CC217725CF"
    def defaultBackupKeyManager         = "com.verknowsys.served.utils.SvdArchiverKeyManager"
    def defaultBackupFileExtension      = "zip.raes"
    def defaultBackupFileMatcher        = """(.*)\.zip\.raes$"""
    def defaultHost                     = "127.0.0.1"
    def defaultDomain                   = "localhost"
    def defaultUserName                 = "guest"
    def defaultUserUID                  = 501
    def defaultSoftwareTemplateExt      = ".json"
    def defaultSoftwareTemplate         = userHomeDir / "Common/Igniters/Default"
    def defaultSoftwareTemplatesDir     = userHomeDir / "Common/Igniters/Services/"
    def remoteApiServerHost             = "10.10.0.1" // 2011-06-09 00:15:00 - dmilith - TODO: XXX: switch to automatic ip detection (one with default route set)
    def defaultAPITimeout               = 15000 // 15 seconds
    def sshPort                         = 22
    def remoteApiServerPort             = 10
    def remoteAccountServerPort         = 12
    def maxSchedulerDefinitions         = 100
    def defaultHttpKeepAliveTimeout     = 65
    def defaultHttpAmountOfWorkers      = 2
    def defaultHttpWorkerConnections    = 1024
    def defaultHttpPort                 = 80 /* NOTE: also make sure that this port is automatically registered to be occupied by http server */
    def defaultHttpsPort                = 443 /* NOTE: also make sure that this port is automatically registered to be occupied by http server */

    def defaultUserGroup                = 0 // from now, every file created for user in filesystem will have 0 uid
    def shutdownTimeout                 = 10000 // 10 seconds to shut down
    def kqueueWaitInterval              = 200
    def sleepDefaultPause               = 1000
    def gatherTimeout                   = 1000
    def serviceRestartPause             = 6000
    def headlessTimeout                 = 6000 // 6 seconds
    def serviceAutostartFile            = ".autostart_service"
    def defaultUserAkkaConf             = "/defaultUserAkkaConf.conf"
    def defaultAkkaConfName             = ".akka.conf"
    def standardShellEnvironment        = ". /etc/profile\nulimit -u 120\n"
    def notificationMailUser            = "notifications"
    def notificationMailPassword        = "mkonjibhu"
    def notificationMailRecipients      = "dmilith@verknowsys.com" :: Nil
    def notificationXmppRecipients      = "dmilith@verknowsys.com" :: "michal.lipski@gmail.com" :: "wick3d@verknowsys.com" :: Nil
    def notificationXmppHost            = "verknowsys.com"
    def notificationXmppLogin           = "notifications"
    def notificationXmppPassword        = "mkonjibhu"
    def notificationXmppResource        = "svd-" + java.util.UUID.randomUUID
    def notificationXmppPort            = 65222
    def notificationXmppCompression     = false
    def notificationXmppUseSasl         = false

    def defaultNtpHost                  = "ntp.task.gda.pl"
    def matcherFQDN                     = """(?=^.{1,254}$)(^(?:(?!\d+\.)[a-zA-Z0-9_\-]{1,63}\.?)+(?:[a-zA-Z]{2,})$)"""

}
