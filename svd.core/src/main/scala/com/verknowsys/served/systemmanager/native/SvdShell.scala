package com.verknowsys.served.systemmanager.native


import com.verknowsys.served._
import com.verknowsys.served.api._
import com.verknowsys.served.utils.Logging
import scala.collection.mutable._
import expectj._


class SvdShellException(reason: String) extends Exception(reason)


class SvdShell(account: SvdAccount, timeout: Int = 0) extends Logging {

    val expectinator = if (timeout > 0)
        new ExpectJ(timeout)
            else
                new ExpectJ

    loadSettings
    val shell = expectinator.spawn(SvdConfig.defaultShell)
    
    
    def loadSettings =
        ". /etc/profile\n" ::
        "export USER=%s\n".format(account.userName) ::
        "export USERNAME=%s\n".format(account.userName) ::
        "export EDITOR=true\n" ::
        "cd %s%s\n".format(SvdConfig.userHomeDir, account.uid) ::
        "ulimit -u 120\n" :: Nil
    
    
    def dead = shell.isClosed
    
    
    def exec(
        command: String,
        expectedStdout: Array[String] = Array(),
        expectedStderr: Array[String] = Array(),
        waitForOutputFor: Int = 2
        ) {
            if (dead) {
                throw new SvdShellException("Failed to exec command: '%s' on dead shell.".format(command))
            } else {
                shell.send(command + "\n")
                if (expectedStdout.size != 0) expectedStdout.foreach {
                    expect =>
                        shell.expect(expect, waitForOutputFor)
                }
                if (expectedStderr.size != 0) expectedStderr.foreach {
                    expect =>
                        shell.expectErr(expect, waitForOutputFor)
                }
            }
    }
    
    
    def output = (shell.getCurrentStandardOutContents, shell.getCurrentStandardErrContents)
    
    
    def close {
        try {
            shell.send("exit\n")
            shell.stop
        } catch {
            case e: Exception =>
                log.error(e.getMessage + " on exit")
        }
    }

    
}
