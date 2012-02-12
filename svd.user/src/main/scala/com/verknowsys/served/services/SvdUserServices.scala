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


    def rackWebAppConfig(account: SvdAccount, domain: SvdUserDomain, name: String = "Passenger") = SvdServiceConfig(
        /*      appRoot     =>  SvdConfig.userHomeDir / account.uid.toString / SvdConfig.webApplicationsDir / domain.name    */
        name = name,

        install = SvdShellOperation(
            "mkdir -p %s ; mkdir -p %s ; cp -r %s %s && echo install".format(
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.webApplicationsDir / domain.name, /* mkdir */
                SvdConfig.userHomeDir / account.uid.toString / SvdConfig.applicationsDir, /* mkdir */
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