/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.api.scheduler

import org.quartz.{Trigger, JobDetail}


object SvdScheduler {

    case class StartJob(name: String, job: JobDetail, trigger: Trigger)
    case class StopJob(name: String)

}
