package com.verknowsys.served.api.scheduler

import org.quartz.{Trigger, JobDetail}


object SvdScheduler {

    case class StartJob(name: String, job: JobDetail, trigger: Trigger)
    case class StopJob(name: String)

}
