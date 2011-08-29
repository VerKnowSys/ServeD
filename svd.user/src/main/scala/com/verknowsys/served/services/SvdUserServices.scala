package com.verknowsys.served.services


import com.verknowsys.served.db._
import com.verknowsys.served.SvdConfig
import com.verknowsys.served.utils._
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.api._


/**
 *  @author dmilith
 *
 *   Definitions of "built in" SvdService configurations, parametrized by SvdAccountManager's account.
 *   NOTE: "name" is additionally name of service root folder.
 */

object SvdUserServices {


     def rackWebAppConfig(account: SvdAccount, domain: SvdUserDomain, name: String = "Passenger") = SvdServiceConfig(
        /*      appRoot     =>  SvdConfig.userHomeDir / account.uid.toString / "WebApps" / domain.name    */
        name = name,

        install = SvdShellOperation(
            "mkdir -p %s ; cp -R %s-** %s && echo install".format(
                SvdConfig.userHomeDir / account.uid.toString / "Apps", /* mkdir */
                SvdConfig.softwareRoot / name,
                SvdConfig.userHomeDir / account.uid.toString / "Apps" / name),
            waitForOutputFor = 90,
            expectStdOut = List("install")) :: Nil,

        validate = SvdShellOperation(
            "test -d %s && test -e %s && test -e %s && echo validation".format(
                SvdConfig.userHomeDir / account.uid.toString / "WebApps" / domain.name,
                SvdConfig.userHomeDir / account.uid.toString / "Apps" / name / "bin" / "ruby",
                SvdConfig.userHomeDir / account.uid.toString / "Apps" / name / "bin" / "gem"),
            waitForOutputFor = 5,
            expectStdOut = List("validation")) :: Nil,

        start = SvdShellOperation(
            "cd %s && %s start -e production -S %s -d && echo start".format( /* XXX: FIXME: HARDCODE: port should be generated, pool size should be automatically set */
                SvdConfig.userHomeDir / account.uid.toString / "WebApps" / domain.name,
                SvdConfig.userHomeDir / account.uid.toString / "Apps" / name / "bin" / "passenger",
                SvdConfig.temporaryDir / "%s-%s.socket".format(domain.name, account.uuid)),
            waitForOutputFor = 90,
            expectStdOut = List("start")) :: Nil,

        stop = SvdShellOperation(
            "cd %s && %s stop --pid-file %s && echo stop".format(
                SvdConfig.userHomeDir / account.uid.toString / "WebApps" / domain.name,
                SvdConfig.userHomeDir / account.uid.toString / "Apps" / name / "bin" / "passenger",
                SvdConfig.userHomeDir / account.uid.toString / "WebApps" / domain.name / "tmp" / "pids" / "passenger.pid"),
            waitForOutputFor = 15,
            expectStdOut = List("stop")) :: Nil

     )


}