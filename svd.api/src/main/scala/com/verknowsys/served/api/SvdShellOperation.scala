package com.verknowsys.served.api


case class SvdShellOperation(
        commands: String,
        env: String = "",
        uuid: UUID = randomUUID
    ) extends Persistent {
        override def toString = "SvdShellOperation(uuid: '%s'; cmd: '%s'; env: '%s')".format(
            uuid,
            commands.split("\n").mkString(", "),
            env.mkString(", ")
            )
    }