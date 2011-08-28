package com.verknowsys.served.services


import com.verknowsys.served.db._
import com.verknowsys.served.SvdConfig
import com.verknowsys.served.utils._
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.api._


object SvdUserServices {


    def passengerDefinitionTemplate(
        account: SvdAccount, defaultDomainName: String = SvdConfig.defaultDomain) =
"""
worker_processes    %s;

events {
    worker_connections  %s;
}

http {
    include     mime.types;
    default_type    application/octet-stream;
    keepalive_timeout   %s;
    gzip    on;

    server {
        listen  %s;
        server_name %s;
        access_log  logs/%s.access.log;
        location    /   {
            root    %s;
            index   index.html  index.htm;
        }
        error_page  500    502    503    504    /50x.html;
        location    =  /50x.html {
            root    %shtml;
        }
    }
}

""".format(
    SvdConfig.defaultHttpAmountOfWorkers, /* worker_processes */
    SvdConfig.defaultHttpWorkerConnections, /* worker_connections */
    SvdConfig.defaultHttpKeepAliveTimeout, /* keepalive_timeout */
    8181, /* listen */ // XXX:, HACK:
    "localhost", /* server name */
    defaultDomainName, /* access log */
    SvdConfig.userHomeDir / "%s".format(account.uid) / "Public", /* root */
    SvdConfig.publicHttpDir /* root */
)

    /**
     *  @author dmilith
     *
     *   Definitions of "built in" SvdService configurations, parametrized by SvdAccountManager's account.
     *   NOTE: "name" is additionally name of service root folder.
     */
     def passengerConfig(account: SvdAccount, name: String = "Passenger") = SvdServiceConfig(

        name = name,

        install = SvdShellOperation(
            "mkdir -p %s ; cp -R %s-** %s && echo install".format(
                    SvdConfig.userHomeDir / account.uid.toString / "Apps", /* mkdir */
                    SvdConfig.softwareRoot / name,
                    SvdConfig.userHomeDir / account.uid.toString / "Apps" / name),
                waitForOutputFor = 90,
                expectStdOut = List("install")) :: Nil,

        configure = SvdShellOperation(
           "mkdir %s ; echo \"%s\" > %s".format(
                SvdConfig.userHomeDir / "%s".format(account.uid) / "Public",
                passengerDefinitionTemplate(account),
                SvdConfig.userHomeDir / "%s".format(account.uid) / "Apps" / name / "conf" / "nginx.conf")) :: Nil,

        validate = SvdShellOperation(
            "%s/sbin/nginx -p %s/ -t".format(
                    SvdConfig.userHomeDir / account.uid.toString / "Apps" / name,
                    SvdConfig.userHomeDir / "%s".format(account.uid) / "Apps" / name,
                    name),
                waitForOutputFor = 10,
                expectStdErr = List("test is successful")) :: Nil,

        start = SvdShellOperation(
            "%s/sbin/nginx -p %s/ && echo start".format(
                    SvdConfig.userHomeDir / account.uid.toString / "Apps" / name,
                    SvdConfig.userHomeDir / "%s".format(account.uid) / "Apps" / name,
                    name),
                waitForOutputFor = 30,
                expectStdOut = List("start")) :: Nil,

        stop = SvdShellOperation(
            "%s/sbin/nginx -p %s/ -s stop && echo stop".format(
                SvdConfig.userHomeDir / account.uid.toString / "Apps" / name,
                SvdConfig.userHomeDir / "%s".format(account.uid) / "Apps" / name,
                name),
            waitForOutputFor = 15,
            expectStdOut = List("stop")) :: Nil

     )


}