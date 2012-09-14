package com.verknowsys.served.utils

import com.google.code.sntpjc.Client


/**
    @author tallica
 */
object SvdNtpSync {


    def setSystemTime(host: String) = {
        try {
            CSystemTime.instance.adjustSystemTime(new Client(host).getLocalOffset)
        }
        catch {
            case e: java.lang.UnsatisfiedLinkError => println(e)
                false
            case e: java.net.SocketTimeoutException => println(e)
                false
            case _ => println("Failed to get current offset time from `%s`.".format(host))
                false
        }
    }


    def apply(host: String = "ntp.task.gda.pl") = {
        setSystemTime(host) match {
            case true =>
                println("Time synchronization succeed.")

            case false =>
                println("Unable to synchronize time.")
        }
    }


}
