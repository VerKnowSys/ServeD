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
    val shellToSpawn = if (account.uid == 0) SvdConfig.defaultShell + " -s" else SvdConfig.defaultShell
    var shell = expectinator.spawn(shellToSpawn)


    def dead = shell.isClosed


    def exec(operations: SvdShellOperations) {
        // spawnThread {
            if (dead) { // if shell is dead, respawn it! It MUST live no matter what
                log.debug("Found dead shell: %s".format(shell))
                shell = expectinator.spawn(shellToSpawn)
                if (dead)
                    throwException[SvdShellException]("Found dead shell where it should be alive!")
            }
            val ops = operations.commands.mkString(" ; ")
            log.trace(s"Executing ${ops} on shell: ${shellToSpawn}")
            shell.send(s"${ops}\n") // send commands one by one to shell

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


    // def output = synchronized {
    //     (shell.getCurrentStandardOutContents, shell.getCurrentStandardErrContents)
    // }


    def close {
        try {
            log.trace("Closing shell. Is it closed? %s".format(shell.isClosed))
            shell.send("\nexit\n")
            shell.stop
            shell.expectClose
            log.debug("Shell closed. Is it really closed? %s".format(shell.isClosed))
        } catch {
            case e: Exception =>
                log.warn("%s on exit from shell.".format(e))
        }
    }


}
