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

    /**
     *  @author dmilith
     *
     *   Generating default empty template including accounts web configuration
     */
    def coreginxEmptyDefinitionTemplate(accountList: List[SvdAccount]) =
"""
worker_processes            %s;

events {
    worker_connections      %s;
    use                     kqueue;
}

http {
    include                 mime.types;
    default_type            application/octet-stream;
    keepalive_timeout       %s;
    keepalive_requests      100;
    charset                 utf-8;
    gzip                    on;
    gzip_http_version       1.1;
    gzip_vary               on;
    gzip_comp_level         4;
    gzip_proxied            any;
    gzip_types              text/plain text/css application/json application/x-javascript text/xml application/xml application/xml+rss text/javascript;
    sendfile                on;
    tcp_nopush              on;
    tcp_nodelay             on;
    ignore_invalid_headers  on;
    recursive_error_pages   on;
    
    # user includes:
    %s
}
""".format(
    SvdConfig.defaultHttpAmountOfWorkers, /* worker_processes */
    SvdConfig.defaultHttpWorkerConnections, /* worker_connections */
    SvdConfig.defaultHttpKeepAliveTimeout, /* keepalive_timeout */
    accountList.map {
        account =>
            "include %s;\n".format(
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.webConfigDir / "*.conf"
            )
    }.mkString("\n")
)


    /**
     *  @author dmilith
     *
     *   Creates Coreginx configuration required to spawn service properly.
     */
    def coreginxConfig(name: String = "Coreginx") = SvdServiceConfig(
        name = name,

        install = SvdShellOperation(
            "mkdir -p %s ; cp -r %s %s && echo install".format(
                    SvdConfig.systemHomeDir / "0" / SvdConfig.applicationsDir, /* mkdir */
                    SvdConfig.softwareRoot / name, SvdConfig.systemHomeDir / "0" / SvdConfig.applicationsDir), /* cp */
                waitForOutputFor = 90,
                expectStdOut = List("install")) :: Nil,

        configure = SvdShellOperation(
            "mkdir -p %s ; chown -R nobody %s && cp -r %s %s && echo \"%s\" > %s".format(
                SvdConfig.publicHttpDir, /* mkdir */
                SvdConfig.publicHttpDir, /* chown */
                SvdConfig.systemHomeDir / "0" / SvdConfig.applicationsDir / name / "html", SvdConfig.publicHttpDir, /* cp */
                coreginxEmptyDefinitionTemplate(SvdAccount(uid = 501) :: Nil), // XXX: hardcode
                // NOTE: devel only
                    // newWebAppEntry(
                    //     SvdUserDomain("localhost"),
                    //     SvdAccount(uid = 501).copy(uuid = java.util.UUID.fromString("7811db03-52f0-4ef8-bcf0-dbc93982315e"))
                    // ) +
                SvdConfig.systemHomeDir / "0" / SvdConfig.applicationsDir / name / "conf" / "nginx.conf" /* echo */
           )) :: Nil,
         
        reload = SvdShellOperation(
            "%s -p %s/ -s reload".format(
                SvdConfig.systemHomeDir / "0" / SvdConfig.applicationsDir / name / "sbin" / "nginx",
                SvdConfig.systemHomeDir / "0" / SvdConfig.applicationsDir / name
            )) :: Nil,

        validate = SvdShellOperation(
            "%s -p %s/ -t".format(
                    SvdConfig.systemHomeDir / "0" / SvdConfig.applicationsDir / name / "sbin" / "nginx",
                    SvdConfig.systemHomeDir / "0" / SvdConfig.applicationsDir / name),
                waitForOutputFor = 10,
                expectStdErr = List("test is successful")) :: Nil,

        start = SvdShellOperation(
            "%s -p %s/ && echo start".format(
                    SvdConfig.systemHomeDir / "0" / SvdConfig.applicationsDir / name / "sbin" / "nginx",
                    SvdConfig.systemHomeDir / "0" / SvdConfig.applicationsDir / name),
                waitForOutputFor = 5,
                expectStdOut = List("start")) :: Nil,

        stop = SvdShellOperation(
            "%s -p %s/ -s stop && echo stop".format(
                    SvdConfig.systemHomeDir / "0" / SvdConfig.applicationsDir / name / "sbin" / "nginx",
                    SvdConfig.systemHomeDir / "0" / SvdConfig.applicationsDir / name),
                waitForOutputFor = 15,
                expectStdOut = List("stop")) :: Nil

    )


}

