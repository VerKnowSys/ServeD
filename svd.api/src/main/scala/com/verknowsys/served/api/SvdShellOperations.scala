/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.api


/**
 *   Defines config of hooks passed to SvdService
 *
 *  @author dmilith
 */
case class SvdServiceConfig(
        name: String,
        softwareName: String = "",
        schedulerActions: List[SvdSchedulerActions],
        install: SvdShellOperations,
        configure: SvdShellOperations,
        start: SvdShellOperations,
        afterStart: SvdShellOperations,
        stop: SvdShellOperations,
        afterStop: SvdShellOperations,
        reload: SvdShellOperations,
        validate: SvdShellOperations,
        staticPort: Int = -1,
        autoStart: Boolean = true,
        autoRestart: Boolean = true, // TODO: Not yet Implemented
        reportAllErrors: Boolean = true,
        reportAllInfos: Boolean = true,
        reportAllDebugs: Boolean = true,
        uuid: UUID = randomUUID
    ) extends Persistent


/**
 *   Defines shell operation required by SvdShell
 *
 *  @author dmilith
 */
case class SvdShellOperations(
        commands: List[String],
        expectStdOut: List[String] = Nil,
        expectStdErr: List[String] = Nil,
        expectOutputTimeout: Int = 0,
        uuid: UUID = randomUUID
    ) extends Persistent {

    override def toString = "Commands: %s, Expecting OUT: '%s', Expecting ERR: '%s', Expected in: %d seconds".format(commands.mkString("' or '"), expectStdOut.mkString("' or '"), expectStdErr.mkString(", "), expectOutputTimeout)

}


case class SvdSchedulerActions(
        cronEntry: String = "* * * * * ?",
        shellCommands: List[String] = Nil,
        jvmCommands: List[String] = Nil,
        uuid: UUID = randomUUID
    ) extends Persistent
