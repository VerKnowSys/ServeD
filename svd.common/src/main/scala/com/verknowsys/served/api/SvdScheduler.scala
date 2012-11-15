package com.verknowsys.served.api.scheduler


import org.quartz._
import org.quartz.impl._


object SvdScheduler {

    case class StartJob(name: String, job: JobDetail, trigger: Trigger)
    case class StopJob(name: String)

}