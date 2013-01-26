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

    val shellToSpawn = if (account.uid == 0) SvdConfig.servedShell + s" --uid=0 --" else SvdConfig.servedShell + " --"
    val shell = expectinator.spawn(shellToSpawn)


    def exec(operations: SvdShellOperations) = synchronized {
        // spawnThread {
            if (shell.isClosed)
                throwException[SvdShellException](s"Found dead shell where it should be alive! It happened with ${operations.commands.mkString(", ")}")
            val ops = operations.commands.mkString(" ; ")
            log.trace(s"Executing ${ops} on shell: ${shellToSpawn}")
            shell.send(s"\n${ops}\n") // send commands one by one to shell

            if (operations.expectStdOut.size != 0) operations.expectStdOut.foreach {
                expect =>
                    shell.expect(expect, operations.expectOutputTimeout)
            }
            if (operations.expectStdErr.size != 0) operations.expectStdErr.foreach {
                expect =>
                    shell.expectErr(expect, operations.expectOutputTimeout)
            }
        // }

    }


    def stdOut = synchronized {
        shell.getCurrentStandardOutContents
    }


    def close = synchronized {
        try {
            log.trace(s"Closing shell. Is it closed? ${shell.isClosed}")
            shell.send("\nexit\n")
            Thread.sleep(2000) // give shell some time to close properly
            shell.stop
            shell.expectClose
            log.debug(s"Shell closed. Is it really closed? ${shell.isClosed}")
        } catch {
            case e: Exception =>
                log.warn(s"${e} on exit from shell."
        }
    }


}
