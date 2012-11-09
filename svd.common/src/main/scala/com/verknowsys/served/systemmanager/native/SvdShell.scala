package com.verknowsys.served.systemmanager.native


import com.verknowsys.served._
import com.verknowsys.served.api._
import com.verknowsys.served.utils._
import scala.collection.mutable._
import expectj._


class SvdShellException(reason: String) extends Exception(reason)


class SvdShell(account: SvdAccount, timeout: Int = 0) extends Logging with SvdUtils {

    val expectinator = if (timeout > 0)
        new ExpectJ(timeout)
            else
                new ExpectJ

    // loadSettings // XXX: UNUSED still
    var shell = expectinator.spawn(SvdConfig.defaultShell)


    def loadSettings =
        "export HOME=%s\n".format(SvdConfig.userHomeDir / "%s".format(account.uid)) ::
        "export USER=%s\n".format(account.uid) ::
        "export USERNAME=%s\n".format(account.uid) ::
        "export EDITOR=true\n" ::
        // "%s\n".format("") ::
        "cd %s%s\n".format(SvdConfig.userHomeDir, account.uid) ::
        SvdConfig.standardShellEnvironment :: Nil


    def dead = shell.isClosed


    def exec(operations: SvdShellOperations) {
        if (dead) { // if shell is dead, respawn it! It MUST live no matter what
            log.debug("Found dead shell: %s".format(shell))
            shell = expectinator.spawn(SvdConfig.defaultShell)
            if (dead)
                throwException[SvdShellException]("Found dead shell where it should be alive!")
        }
        operations.commands.foreach {
            cmd =>
                shell.send(cmd + "\n") // send commands one by one to shell
        }

        if (operations.expectStdOut.size != 0) operations.expectStdOut.foreach {
            expect =>
                shell.expect(expect, operations.expectOutputTimeout)
        }
        if (operations.expectStdErr.size != 0) operations.expectStdErr.foreach {
            expect =>
                shell.expectErr(expect, operations.expectOutputTimeout)
        }
    }


    def output = (shell.getCurrentStandardOutContents, shell.getCurrentStandardErrContents)


    def close {
        try {
            log.trace("Stopping shell %s. Shell is closed?: %s".format(shell, shell.isClosed))
            shell.send("\nexit\n")
            shell.stop
        } catch {
            case e: Exception =>
                log.warn("%s on exit from shell.".format(e))
        }
    }


}
