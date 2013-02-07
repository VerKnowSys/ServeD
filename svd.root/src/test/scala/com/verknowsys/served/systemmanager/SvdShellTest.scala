/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.systemmanager


import com.verknowsys.served.testing._
import com.verknowsys.served.api._
import com.verknowsys.served.systemmanager.native._

import java.lang.{System => JSystem}
import java.util.concurrent.TimeoutException


class SvdShellTest extends DefaultTest {


    def shr(op: String, env: String = "", expectOutput: List[String] = Nil, expectOutput: List[String] = Nil, waitForOutputFor: Int = 5) = SvdShellOperations(List(op), expectOutput, expectOutput, waitForOutputFor)


    it should "spawn command properly and know when it's dead and throw proper exception when shell is dead" in {
        val sh = new SvdShell(
            new SvdAccount(
                userName = JSystem.getProperty("user.name"),
                uid = randomPort
            )
        )

        sh.exec(shr(""))
        sh.dead should be(false)
        sh.close
        sh.dead should be(true)
    }


    it should "get proper output from shell" in {

        val sh = new SvdShell(
            new SvdAccount(
                userName = JSystem.getProperty("user.name"),
                uid = randomPort
            )
        )

        sh.exec(shr("ls -m /dev", expectOutput = List("null", "zero"), waitForOutputFor = 2))

        evaluating {
            sh.exec(shr("ls -m /dev", expectOutput = List("somethingNonExistant"), waitForOutputFor = 1))
        } should produce [TimeoutException]

        evaluating {
            sh.exec(shr("ls -m /dev", expectOutput = List("somethingNonExistant"), waitForOutputFor = 1))
        } should produce [TimeoutException]

        sh.close
    }


    it should "be able to get return code from ran processes" in {
        val sh = new SvdShell(
            new SvdAccount(
                userName = JSystem.getProperty("user.name"),
                uid = randomPort
            )
        )
        sh.exec(shr("lsdjf"))
        sh.exec(shr("echo $?", expectOutput = List("127"))) // NOTE: 127 - command not found code from shell
        sh.exec(shr("ls /nonexistantSomethingBlaBla"))
        sh.exec(shr("echo $?", expectOutput = List("1"))) // NOTE: 1 - error thrown from ls command
        sh.exec(shr("ls"))
        sh.exec(shr("echo $?", expectOutput = List("0")))
        sh.close
    }


}
