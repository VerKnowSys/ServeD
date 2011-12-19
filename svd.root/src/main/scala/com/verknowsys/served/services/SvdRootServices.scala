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


    /**
     *  @author dmilith
     *
     *   Add entry about proxy on user's web app on given port
     */
    def newWebAppEntry(domain: SvdUserDomain, port: SvdUserPort) = """
# ENTRY %s
server {
    listen %s;
    server_name %s %s;
    location / {
        proxy_pass http://127.0.0.1:%s;
    }
}
# ENTRY END
""".format(
    domain.name,
    SvdConfig.defaultHttpPort, /* listen */
    domain.name, /* main server_name */
    if (domain.wildcard) /* wildcard */
        "*.%s".format(domain.name)
    else
        "",
    port.number
)


    /**
     *  @author dmilith
     *
     *   Add entry about proxy on user's web app through unix socket (default)
     */
    def newWebAppEntry(domain: SvdUserDomain, account: SvdAccount) = """
# ENTRY %s
server {
    listen %s;
    server_name %s %s;
    location / {
        proxy_pass http://%s;
    }
}
# ENTRY END
""".format(
    domain.name,
    SvdConfig.defaultHttpPort, /* listen */
    domain.name, /* main server_name */
    if (domain.wildcard) /* wildcard */
        "*.%s".format(domain.name)
    else
        "",
    "unix:/tmp/%s-%s.socket".format(domain.name, account.uuid)
)


    /**
     *  @author dmilith
     *
     *   Creates Coreginx configuration required to spawn service properly.
     */
    def coreginxConfig(name: String = "Coreginx") = SvdServiceConfig(
        name = name,

        install = SvdShellOperation(
            "mkdir -p %s ; cp -R %s** %s && echo install".format(
                    SvdConfig.systemHomeDir / "0" / SvdConfig.applicationsDir, /* mkdir */
                    SvdConfig.softwareRoot / name, SvdConfig.systemHomeDir / "0" / SvdConfig.applicationsDir / name), /* cp */
                waitForOutputFor = 90,
                expectStdOut = List("install")) :: Nil,

        configure = SvdShellOperation(
           "mkdir -p %s ; chown -R nobody %s && cp -r %s %s && echo \"%s\" > %s".format(
                SvdConfig.publicHttpDir, /* mkdir */
                SvdConfig.publicHttpDir, /* chown */
                SvdConfig.systemHomeDir / "0" / SvdConfig.applicationsDir / name / "html", SvdConfig.publicHttpDir, /* cp */
                coreginxDefinitionTemplate() +

// NOTE: devel only
                newWebAppEntry(
                    SvdUserDomain("localhost"),
                    SvdAccount(uid = 501).copy(uuid = java.util.UUID.fromString("7811db03-52f0-4ef8-bcf0-dbc93982315e"))
                ) +
                "\n}", // XXX: HACK: NOTE: closing base "server" clause. STILL HACK

                SvdConfig.systemHomeDir / "0" / SvdConfig.applicationsDir / name / "conf" / "nginx.conf" /* echo */
           )) :: Nil,

        validate = SvdShellOperation(
            "%s0/Apps/%s/sbin/nginx -p %s/ -t".format(
                    SvdConfig.systemHomeDir,
                    name,
                    SvdConfig.systemHomeDir / "0" / SvdConfig.applicationsDir / name),
                waitForOutputFor = 10,
                expectStdErr = List("test is successful")) :: Nil,

        start = SvdShellOperation(
            "%s0/Apps/%s/sbin/nginx -p %s/ && echo start".format(
                    SvdConfig.systemHomeDir,
                    name,
                    SvdConfig.systemHomeDir / "0" / SvdConfig.applicationsDir / name),
                waitForOutputFor = 5,
                expectStdOut = List("start")) :: Nil,

        stop = SvdShellOperation(
            "%s0/Apps/%s/sbin/nginx -p %s/ -s stop && echo stop".format(
                    SvdConfig.systemHomeDir,
                    name,
                    SvdConfig.systemHomeDir / "0" / SvdConfig.applicationsDir / name),
                waitForOutputFor = 15,
                expectStdOut = List("stop")) :: Nil

    )


}

