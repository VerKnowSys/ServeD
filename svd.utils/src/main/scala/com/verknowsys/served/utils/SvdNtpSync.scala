package com.verknowsys.served.utils

import com.verknowsys.served._
import com.google.code.sntpjc.Client


/**
    @author tallica
 */
object SvdNtpSync extends Logging {


    def setSystemTime(host: String) = {
        try {
            CSystemTime.instance.adjustSystemTime(new Client(host).getLocalOffset)
        }
        catch {
            case e: java.lang.UnsatisfiedLinkError => log.error("%s\n".format(e))
                false
            case e: java.net.SocketTimeoutException => log.error("%s\n".format(e))
                false
            case _ => log.error("Failed to get current offset time from `%s`.".format(host))
                false
        }
    }


    def apply(host: String = SvdConfig.defaultNtpHost) = {
        setSystemTime(host) match {
            case true =>
                log.info("Time synchronization succeed.")

            case false =>
                log.warn("Unable to synchronize time.")
        }
    }


}
