/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.ci


/** 
 * Abstract container for svd.ci tasks
 * 
 * The whole idea is to abstract each task into (case) class/object to provide unified interface
 * As of today (2.02.2011) sbt (0.7.4) is built using scala 2.7 
 * so it is impossible to execute tasks programically
 * and they must be run as separate process. In future, when sbt switch to scala 2.8 
 * it might be possible to build projects without executing external processes
 * 
 * @param cmd command that will be run
 * @author teamon
 */
abstract class Task(val cmd: String)

object Task {
    case object Clean extends Task("sbt clean")
    case object Compile extends Task("sbt compile")
    case object Update extends Task("sbt update")
    case object Test extends Task("sbt test")
}
