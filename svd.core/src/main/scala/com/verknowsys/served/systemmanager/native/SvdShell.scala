package com.verknowsys.served.systemmanager.native


import com.verknowsys.served._
import com.verknowsys.served.api._
import com.verknowsys.served.utils.Logging
import scala.collection.mutable._
import expectj._


class SvdShell(account: SvdAccount, timeout: Int = 0) extends Logging {

    val expectinator = if (timeout > 0)
        new ExpectJ(timeout)
            else
                new ExpectJ
    val shells: ArrayBuffer[Spawn] = ArrayBuffer[Spawn](startShell)


    def startShell = expectinator.spawn(SvdConfig.defaultShell) // 2011-06-19 02:27:55 - dmilith - XXX: hardcoded shell
    
    
    def loadSettings =
        ". /etc/profile\n" ::
        "export USER=%s\n".format(account.userName) ::
        "export USERNAME=%s\n".format(account.userName) ::
        "export EDITOR=true\n" ::
        "cd %s%s\n".format(SvdConfig.userHomeDir, account.uid) ::
        "ulimit -u 120\n" :: Nil
    
    
    def dead(shellNumber: Int = 0) = shells(shellNumber).isClosed
    
    
    def exec(command: String, expected: String = "", shellNumber: Int = 0) = {
        try {
            if (dead(shellNumber)) {
                throw new Exception("Failed to exec (dead)")
            } else {
                loadSettings.foreach{s => shells(shellNumber).send(s)} // load settings
                shells(shellNumber).send(command + "\n")
                if (expected != "")
                    shells(shellNumber).expect(expected)
            }
        } catch {
            case e: Exception =>
                log.error(e.getMessage + " shell: " + shellNumber + " with command: '" + command + "'. Expected: '" + expected + "'")
        }
    }
    
    
    def output(shellNumber: Int = 0) = shells(shellNumber).getCurrentStandardOutContents :: shells(shellNumber).getCurrentStandardErrContents :: Nil
    
    
    def close(shellNumber: Int = 0) {
        try {
            shells(shellNumber).send("exit\n")
            shells.remove(shellNumber)
            if (shells.isEmpty) {
                log.debug("Empty shell list detected.")
            }
        } catch {
            case e: Exception =>
                log.error(e.getMessage + " on exit")
        }
    }
    
    
    def closeAll = shells.foreach{s => s.stop}
    
}
