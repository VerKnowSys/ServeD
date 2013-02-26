/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.systemmanager.native


import com.verknowsys.served._
import com.verknowsys.served.api._
import com.verknowsys.served.utils._
import expectj.ExpectJ


class SvdShellException(reason: String) extends Exception(reason)


class SvdShell(account: SvdAccount, timeout: Int = 0) extends Logging with SvdUtils {

    val expectinator = if (timeout > 0)
        new ExpectJ(timeout)
            else
                new ExpectJ

    log.debug(s"Spawning user Shell for account ${account}")

    val shell = expectinator.spawn(SvdConfig.servedShell)


    def exec(operations: SvdShellOperations) = {
        // spawnThread {
        if (shell.isClosed)
            throwException[SvdShellException](s"Found dead shell where it should be alive! It happened with ${operations.commands.mkString(", ")}")
        val ops = operations.commands.mkString(" ; ")
        if (ops.isEmpty) {
            log.debug("Empty shell operations. Skipping execution")
        } else {
            log.trace(s"Executing ${ops} on shell: ${SvdConfig.servedShell}")
            shell.send(s"${ops}\n") // send commands one by one to shell

            if (operations.expectOutput.size != 0) operations.expectOutput.foreach {
                expect =>
                    shell.expect(expect, operations.expectOutputTimeout)
            }
        }

    }


    def stdOut = {
        shell.getCurrentStandardOutContents
    }


    def close = {
        try {
            log.trace(s"Closing shell. Is it closed? ${shell.isClosed}")
            shell.stop
            shell.expectClose
            log.debug(s"Shell closed. Is it really closed? ${shell.isClosed}")
        } catch {
            case e: Exception =>
                log.debug(s"Thrown ${e} on exit from shell.")
        }
    }


}
