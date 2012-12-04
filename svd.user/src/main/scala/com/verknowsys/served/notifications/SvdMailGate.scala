package com.verknowsys.served.notifications


import com.verknowsys.served._
import com.verknowsys.served.utils._


class SvdMailGate extends Gate with Logging with SvdUtils {

    def connect {
        log.info("Initiating SvdMailGate")
    }

    def disconnect {

    }

    def setStatus(st: String) {
    }

    def send(message: String) {
        log.debug("NYI! Sending eMail")
        // SvdNotifyMailer(message, SvdConfig.notificationMailRecipients)
    }


}