package com.verknowsys.served.api

/**
 *  @author dmilith
 *
 *   Defines config of hooks passed to SvdService
 */
case class SvdServiceConfig(
        name: String,
        install: List[SvdShellOperation] = Nil,
        configure: List[SvdShellOperation] = Nil,
        start: List[SvdShellOperation] = Nil,
        afterStart: List[SvdShellOperation] = Nil,
        stop: List[SvdShellOperation] = Nil,
        afterStop: List[SvdShellOperation] = Nil,
        reload: List[SvdShellOperation] = Nil,
        validate: List[SvdShellOperation] = Nil,
        uuid: UUID = randomUUID
    ) extends Persistent


/**
 *  @author dmilith
 *
 *   Defines shell operation required by SvdShell
 */
case class SvdShellOperation(
        commands: String,
        env: String = "",
        expectStdOut: List[String] = Nil,
        expectStdErr: List[String] = Nil,
        waitForOutputFor: Int = 5, /* waiting 5 seconds until operation is done */
        uuid: UUID = randomUUID
    ) {
        override def toString = "SvdShellOperation(cmd: '%s'; env: '%s')".format(
            commands.split("\n").mkString(", "),
            env.mkString(", ")
            )
    }
