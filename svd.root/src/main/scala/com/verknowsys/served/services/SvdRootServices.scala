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


    def coreginxDefinitionTemplate(
        defaultDomainName: String = SvdConfig.defaultDomain) =
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
            root    %shtml;
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
    SvdConfig.defaultHttpPort, /* listen */
    defaultDomainName, /* server name */
    defaultDomainName, /* access log */
    SvdConfig.publicHttpDir, /* root */
    SvdConfig.publicHttpDir /* root */
)


    def coreginxConfig(name: String = "Coreginx") = SvdServiceConfig(
        name = name,

        install = SvdShellOperation(
            "mkdir -p %s0/Apps ; cp -R %s%s** %s0/Apps/%s && echo install".format(
                    SvdConfig.systemHomeDir, /* mkdir */
                    SvdConfig.softwareRoot, name, SvdConfig.systemHomeDir, name), /* cp */
                waitForOutputFor = 90,
                expectStdOut = List("install")) :: Nil,

        configure = SvdShellOperation(
           "mkdir -p %s ; chown -R nobody %s && cp -r %s %s && echo \"%s\" > %s".format(
                SvdConfig.publicHttpDir, /* mkdir */
                SvdConfig.publicHttpDir, /* chown */
                SvdConfig.systemHomeDir / "0" / "Apps" / name / "html", SvdConfig.publicHttpDir, /* cp */
                coreginxDefinitionTemplate(), SvdConfig.systemHomeDir / "0" / "Apps" / name / "conf" / "nginx.conf" /* echo */
           )) :: Nil,

        validate = SvdShellOperation(
            "test -d %s0/Apps/%s && echo validation".format(
                    SvdConfig.systemHomeDir,
                    name),
                waitForOutputFor = 60,
                expectStdOut = List("validation")) :: Nil,

        start = SvdShellOperation(
            "%s0/Apps/%s/sbin/nginx -p %s && echo start".format(
                    SvdConfig.systemHomeDir,
                    name,
                    SvdConfig.systemHomeDir / "0" / "Apps" / name + "/"),
                waitForOutputFor = 5,
                expectStdOut = List("start")) :: Nil,

        stop = SvdShellOperation(
            "%s0/Apps/%s/sbin/nginx -p %s -s stop && echo stop".format(
                    SvdConfig.systemHomeDir,
                    name,
                    SvdConfig.systemHomeDir / "0" / "Apps" / name + "/"),
                waitForOutputFor = 15,
                expectStdOut = List("stop")) :: Nil

    )


}

