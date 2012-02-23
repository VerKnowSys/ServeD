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


    def postgresDatabaseConfig(account: SvdAccount, name: String = "Postgresql") = SvdServiceConfig(
        name = name,

        install = SvdShellOperation(
            "mkdir -p %s ; cp -r %s %s && %s -D %s && echo install".format(
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.applicationsDir, /* mkdir */
                SvdConfig.softwareRoot / name,
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.applicationsDir, /* cp */
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.applicationsDir / name / "bin" / "initdb",
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.applicationsDir / name / name /* data folder */
            ),
            waitForOutputFor = 120,
            expectStdOut = List("install")) :: Nil,

        validate = SvdShellOperation(
            "test -x %s && test -x %s && test -x %s && echo validation".format(
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.applicationsDir / name / "bin" / "initdb",
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.applicationsDir / name / "bin" / "pg_ctl",
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.applicationsDir / name / "bin" / "postgres"
            ),
            waitForOutputFor = 5,
            expectStdOut = List("validation")) :: Nil,

        configure = SvdShellOperation(
            "mkdir -p %s ; echo \"%s\" > \"%s\" && echo \"%s\" > \"%s\" && echo configuration".format(
                SvdConfig.temporaryDir / "%s-%s".format(name, account.uuid),
                """
local all all trust
host replication all 0.0.0.0/0 trust
                """,
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.applicationsDir / name / name / "pg_hba.conf", /* pg_hba.conf */
                """
unix_socket_directory '%s'
max_connections = 10
checkpoint_segments = 24
password_encryption = on
datestyle = 'iso, mdy'
shared_buffers = 64MB
temp_buffers = 32MB
work_mem = 16MB
max_stack_depth = 16MB

logging_collector = true
log_directory = '%s'
client_encoding = 'UTF-8'
lc_messages = 'pl_PL.UTF-8'
lc_monetary = 'pl_PL.UTF-8'
lc_numeric = 'pl_PL.UTF-8'
lc_time = 'pl_PL.UTF-8'
default_text_search_config = 'pg_catalog.polish'

# master for host standby, streaming replication:
wal_level = hot_standby
max_wal_senders = 5
wal_keep_segments = 32
archive_mode = on
                """.format(
                    SvdConfig.temporaryDir / "%s-%s".format(name, account.uuid),
                    SvdConfig.temporaryDir / "%s-%s".format(name, account.uuid) // NOTE: consider log in app dir
                ),
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.applicationsDir / name / name / "postgresql.conf" /* postgresql.conf */
            ),
            waitForOutputFor = 15,
            expectStdOut = List("configuration")) :: Nil,

        start = SvdShellOperation(
            "%s -m fast -D %s start && echo start".format(
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.applicationsDir / name / "bin" / "pg_ctl",
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.applicationsDir / name / name /* data folder */
            ),
            waitForOutputFor = 90,
            expectStdOut = List("start")) :: Nil,

        stop = SvdShellOperation(
            "%s -m fast -D '%s' stop && echo stop".format(
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.applicationsDir / name / "bin" / "pg_ctl",
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.applicationsDir / name / name /* data folder */
            ),
            waitForOutputFor = 15,
            expectStdOut = List("stop")) :: Nil

    )


    def phpWebAppConfig(account: SvdAccount, domain: SvdUserDomain, name: String = "Php") = SvdServiceConfig(
        name = name,
        install = SvdShellOperation(
            "mkdir -p %s ; mkdir -p %s ; mkdir -p %s ; cp -r %s %s && echo install".format(
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.webApplicationsDir / domain.name, /* mkdir */
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.applicationsDir, /* mkdir */
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.webConfigDir, /* mkdir */
                SvdConfig.softwareRoot / name,
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.applicationsDir),
                waitForOutputFor = 120,
                expectStdOut = List("install")) :: Nil,

        configure = SvdShellOperation(
            "echo \"%s\" > \"%s\" && echo \"%s\" > \"%s\" && echo configuration".format(
                """
[global]
    pid = %s/php5-fpm.pid
    error_log = %s/php5-fpm.log
    ;syslog.facility = daemon
    ;syslog.ident = php-fpm
    ;log_level = notice
    ;emergency_restart_threshold = 0
    ;emergency_restart_interval = 0
    ;process_control_timeout = 0
    ; process.max = 128
    ;daemonize = yes
    ;rlimit_files = 1024
    ;rlimit_core = 0
    events.mechanism = kqueue
    ;security.limit_extensions = .php .html .htm .less .js .coffee
[www]
    user = nouser
    listen = %s
    pm = dynamic
    pm.max_children = 5
    pm.start_servers = 2
    pm.min_spare_servers = 1
    pm.max_spare_servers = 3
    ;pm.process_idle_timeout = 10s;
    ;pm.max_requests = 500
                 """.format(
                    SvdConfig.userHomeDir / account.uid.toString / SvdConfig.applicationsDir / name / "var/run",
                    SvdConfig.userHomeDir / account.uid.toString / SvdConfig.applicationsDir / name / "var/log",
                    SvdConfig.publicHttpDir / account.uuid.toString + "-" + name + "-" + domain.name +  "-" + "php-fpm.sock" // socket file
                    ),
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.applicationsDir / name / "etc" / "php-fpm.conf", /* php-fpm.conf */
                """
[PHP]
    engine = On
    short_open_tag = Off
    asp_tags = Off
    precision = 14
    y2k_compliance = On
    output_buffering = 4096
    zlib.output_compression = Off
    implicit_flush = Off
    unserialize_callback_func =
    serialize_precision = 100
    allow_call_time_pass_reference = Off
    safe_mode = Off
    safe_mode_gid = Off
    safe_mode_include_dir =
    safe_mode_exec_dir =
    safe_mode_allowed_env_vars = PHP_
    safe_mode_protected_env_vars = LD_LIBRARY_PATH
    open_basedir = %s
    disable_functions = exec,popen,system
    disable_classes =
    realpath_cache_size = 128k
    expose_php = Off
    max_execution_time = 30
    max_input_time = 60
    memory_limit = 512M
    error_reporting = E_ALL & ~E_DEPRECATED
    display_errors = On
    display_startup_errors = Off
    log_errors = On
    log_errors_max_len = 1024
    ignore_repeated_errors = Off
    ignore_repeated_source = Off
    report_memleaks = On
    track_errors = Off
    html_errors = Off
    variables_order = "GPCS"
    request_order = "GP"
    register_globals = Off
    register_long_arrays = Off
    register_argc_argv = Off
    auto_globals_jit = On
    post_max_size = 8M
    magic_quotes_gpc = On
    magic_quotes_runtime = Off
    magic_quotes_sybase = Off
    auto_prepend_file =
    auto_append_file =
    default_mimetype = "text/html"
    default_charset = "UTF-8"
    doc_root =
    user_dir = %s
    enable_dl = Off
    file_uploads = On
    upload_max_filesize = 50M
    max_file_uploads = 20
    allow_url_fopen = On
    allow_url_include = Off
    default_socket_timeout = 60
[Pdo_mysql]
    pdo_mysql.cache_size = 2000
    pdo_mysql.default_socket=
[Syslog]
    define_syslog_variables  = Off
[mail function]
    SMTP = localhost
    smtp_port = 25
    mail.add_x_header = On
[SQL]
    sql.safe_mode = Off
[ODBC]
    odbc.allow_persistent = On
    odbc.check_persistent = On
    odbc.max_persistent = -1
    odbc.max_links = -1
    odbc.defaultlrl = 4096
    odbc.defaultbinmode = 1
[Interbase]
    ibase.allow_persistent = 1
    ibase.max_persistent = -1
    ibase.max_links = -1
[MySQL]
    mysql.allow_local_infile = On
    mysql.allow_persistent = On
    mysql.cache_size = 2000
    mysql.max_persistent = -1
    mysql.max_links = -1
    mysql.default_port =
    mysql.default_socket =
    mysql.default_host =
    mysql.default_user =
    mysql.default_password =
    mysql.connect_timeout = 60
    mysql.trace_mode = Off
[MySQLi]
    mysqli.max_persistent = -1
    mysqli.allow_persistent = On
    mysqli.max_links = -1
    mysqli.cache_size = 2000
    mysqli.default_port = 3306
    mysqli.default_socket =
    mysqli.default_host =
    mysqli.default_user =
    mysqli.default_pw =
    mysqli.reconnect = Off
[mysqlnd]
    mysqlnd.collect_statistics = On
    mysqlnd.collect_memory_statistics = Off
[OCI8]
    pgsql.allow_persistent = On
    pgsql.auto_reset_persistent = Off
    pgsql.max_persistent = -1
    pgsql.max_links = -1
    pgsql.ignore_notice = 0
    pgsql.log_notice = 0
    sybct.allow_persistent = On
    sybct.max_persistent = -1
    sybct.max_links = -1
    sybct.min_server_severity = 10
    sybct.min_client_severity = 10
    bcmath.scale = 0
    session.save_handler = files
    session.use_cookies = 1
    session.use_only_cookies = 1
    session.name = PHPSESSID
    session.auto_start = 0
    session.cookie_lifetime = 0
    session.cookie_path = /
    session.cookie_domain =
    session.cookie_httponly =
    session.serialize_handler = php
    session.gc_probability = 1
    session.gc_divisor = 1000
    session.gc_maxlifetime = 1440
    session.bug_compat_42 = Off
    session.bug_compat_warn = Off
    session.referer_check =
    session.entropy_length = 0
    session.cache_limiter = nocache
    session.cache_expire = 180
    session.use_trans_sid = 0
    session.hash_function = 0
    session.hash_bits_per_character = 5
    url_rewriter.tags = "a=href,area=href,frame=src,input=src,form=fakeentry"
[MSSQL]
    mssql.allow_persistent = On
    mssql.max_persistent = -1
    mssql.max_links = -1
    mssql.min_error_severity = 10
    mssql.min_message_severity = 10
    mssql.compatability_mode = Off
    mssql.secure_connection = Off
[Tidy]
    tidy.clean_output = Off
[soap]
    soap.wsdl_cache_enabled=1
    soap.wsdl_cache_dir="/tmp"
    soap.wsdl_cache_ttl=86400
    soap.wsdl_cache_limit = 5
[ldap]
    ldap.max_links = -1
[dba]
    apc.include_once_override = Off
    apc.canonicalize = On
                """.format(
                    SvdConfig.userHomeDir / account.uid.toString / SvdConfig.webApplicationsDir / domain.name, // open_basedir
                    SvdConfig.userHomeDir / account.uid.toString / SvdConfig.webApplicationsDir / domain.name // user_dir
                    // ) // NOTE: consider log in app dir
                ),
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.applicationsDir / name / "etc" / "php.ini" /* php.ini */
            ),
            waitForOutputFor = 15,
            expectStdOut = List("configuration")) :: Nil,

                
        validate = SvdShellOperation(
            "test -d %s && test -x %s && test -x %s && echo validation".format(
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.webApplicationsDir / domain.name,
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.applicationsDir / name / "sbin" / "php-fpm",
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.applicationsDir / name / "bin" / "php-config"),
            waitForOutputFor = 5,
            expectStdOut = List("validation")) :: Nil,
        
        start = SvdShellOperation(
            "%s -p %s && echo start".format(
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.applicationsDir / name / "sbin" / "php-fpm",
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.applicationsDir / name),
            waitForOutputFor = 90,
            expectStdOut = List("start")) :: Nil,

        stop = SvdShellOperation(
            "kill $(cat %s) && echo stop".format(
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.applicationsDir / name / "var/run" / "php5-fpm.pid"
                ),
            waitForOutputFor = 15,
            expectStdOut = List("stop")) :: Nil
        
        )
    

    def rackWebAppConfig(account: SvdAccount, domain: SvdUserDomain, name: String = "Passenger") = SvdServiceConfig(
        /*      appRoot     =>  SvdConfig.userHomeDir / account.uid.toString / SvdConfig.webApplicationsDir / domain.name    */
        name = name,

        install = SvdShellOperation(
            "mkdir -p %s ; mkdir -p %s ; mkdir -p %s ; cp -r %s %s && echo install".format(
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.webApplicationsDir / domain.name, /* mkdir */
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.applicationsDir, /* mkdir */
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.webConfigDir, /* mkdir */
                SvdConfig.softwareRoot / name,
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.applicationsDir),
            waitForOutputFor = 120,
            expectStdOut = List("install")) :: Nil,

        validate = SvdShellOperation(
            "test -d %s && test -x %s && test -x %s && echo validation".format(
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.webApplicationsDir / domain.name,
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.applicationsDir / name / "bin" / "ruby",
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.applicationsDir / name / "bin" / "gem"),
            waitForOutputFor = 5,
            expectStdOut = List("validation")) :: Nil,

        start = SvdShellOperation(
            "cd %s && %s start -e production -S %s -d && echo start".format( /* XXX: FIXME: HARDCODE: port should be generated, pool size should be automatically set */
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.webApplicationsDir / domain.name,
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.applicationsDir / name / "bin" / "passenger",
                SvdConfig.temporaryDir / "%s-%s.socket".format(domain.name, account.uuid)),
            waitForOutputFor = 90,
            expectStdOut = List("start")) :: Nil,

        stop = SvdShellOperation(
            "cd %s && %s stop --pid-file %s && echo stop".format(
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.webApplicationsDir / domain.name,
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.applicationsDir / name / "bin" / "passenger",
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.webApplicationsDir / domain.name / "tmp" / "pids" / "passenger.pid"),
            waitForOutputFor = 15,
            expectStdOut = List("stop")) :: Nil

     )


}