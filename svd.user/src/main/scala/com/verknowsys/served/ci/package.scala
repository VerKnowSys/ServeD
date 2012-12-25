/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.ci

case object Build
case class ProcessFinished(exitCode: Int, stdout: String, stderr: String)

abstract sealed class BuildResult(history: List[ProcessFinished])
case class BuildFailed(history: List[ProcessFinished]) extends BuildResult(history)
case class BuildSucceed(history: List[ProcessFinished]) extends BuildResult(history)
