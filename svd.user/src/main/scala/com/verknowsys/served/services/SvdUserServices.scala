package com.verknowsys.served.services


import com.verknowsys.served.db._
import com.verknowsys.served.SvdConfig
import com.verknowsys.served.utils._
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.api._


object SvdUserServices {

    /**
     *  @author dmilith
     *
     *   Definitions of "built in" SvdService configurations, parametrized by SvdAccountManager's account.
     *   NOTE: "name" is additionally name of service root folder.
     */
     def passengerConfig(account: SvdAccount, name: String = "Passenger") = SvdServiceConfig(

         name = name,

         install = SvdShellOperation(
            "mkdir -p %s/Apps ; cp -R %s%s-** %s/Apps/%s && echo install".format(
                    SvdConfig.userHomeDir / account.uid.toString,
                    SvdConfig.softwareRoot,
                    name,
                    SvdConfig.userHomeDir / account.uid.toString,
                    name),
                waitForOutputFor = 90,
                expectStdOut = List("install")) :: Nil,

         // configure = SvdShellOperation() :: Nil,

         validate = SvdShellOperation(
            "test -e %s/Apps/%s/sbin/nginx && echo validate".format(
                    SvdConfig.userHomeDir / account.uid.toString,
                    name),
                waitForOutputFor = 60,
                expectStdOut = List("validate")) :: Nil,

         start = SvdShellOperation(
            "%s/Apps/%s/sbin/nginx && echo start".format(
                    SvdConfig.userHomeDir / account.uid.toString,
                    name),
                waitForOutputFor = 30,
                expectStdOut = List("start")) :: Nil,

         stop = SvdShellOperation(
            "%s/Apps/%s/sbin/nginx -s stop && echo stop".format(
                SvdConfig.userHomeDir / account.uid.toString,
                name),
            waitForOutputFor = 15,
            expectStdOut = List("stop")) :: Nil

     )


}