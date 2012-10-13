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
        "export USER=%s\n".format(account.userName) ::
        "export USERNAME=%s\n".format(account.userName) ::
        "export EDITOR=true\n" ::
        "%s\n".format("") ::
        "cd %s%s\n".format(SvdConfig.userHomeDir, account.uid) ::
        SvdConfig.standardShellEnvironment :: Nil


    def dead = shell.isClosed


    def exec(operation: SvdShellOperation) {
        if (dead) { // if shell is dead, respawn it! It MUST live no matter what
            log.trace("Found dead shell: %s".format(shell))
            shell = expectinator.spawn(SvdConfig.defaultShell)
            if (dead)
                throwException[SvdShellException]("Found dead shell where it should be alive!")
        }
        shell.send(operation.commands + "\n")
        if (operation.expectStdOut.size != 0) operation.expectStdOut.foreach {
            expect =>
                shell.expect(expect, operation.waitForOutputFor)
        }
        if (operation.expectStdErr.size != 0) operation.expectStdErr.foreach {
            expect =>
                shell.expectErr(expect, operation.waitForOutputFor)
        }
    }


    def output = (shell.getCurrentStandardOutContents, shell.getCurrentStandardErrContents)


    def close {
        try {
            log.trace("Stopping shell %s. Shell is closed?: %s".format(shell, shell.isClosed))
            shell.send("exit\n")
            shell.stop
        } catch {
            case e: Exception =>
                log.error(e.getMessage + " on exit")
        }
    }


}
