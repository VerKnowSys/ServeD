package com.verknowsys.served.services


import com.verknowsys.served.db._
import com.verknowsys.served.SvdConfig
import com.verknowsys.served.utils._
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.api._


/**
 *  @author dmilith
 *
 *   Predefined root services
 */
object SvdRootServices {


    def coreginxConfig(name: String = "Coreginx") = SvdServiceConfig(
        name = name,

        install = SvdShellOperation(
            "mkdir -p %s0/Apps ; cp -R %s%s** %s0/Apps/%s && echo install".format(
                    SvdConfig.systemHomeDir,
                    SvdConfig.softwareRoot,
                    name,
                    SvdConfig.systemHomeDir,
                    name),
                waitForOutputFor = 90,
                expectStdOut = List("install")) :: Nil,

        // configure = SvdShellOperation() :: Nil,

        validate = SvdShellOperation(
            "test -d %s0/Apps/%s && echo validation".format(
                    SvdConfig.systemHomeDir,
                    name),
                waitForOutputFor = 60,
                expectStdOut = List("validation")) :: Nil,

        start = SvdShellOperation(
            "%s0/Apps/%s/sbin/nginx && echo start".format(
                    SvdConfig.systemHomeDir,
                    name),
                waitForOutputFor = 30,
                expectStdOut = List("start")) :: Nil,

        stop = SvdShellOperation(
            "%s0/Apps/%s/sbin/nginx -s stop && echo stop".format(
                    SvdConfig.systemHomeDir,
                    name),
                waitForOutputFor = 15,
                expectStdOut = List("stop")) :: Nil

    )


}

