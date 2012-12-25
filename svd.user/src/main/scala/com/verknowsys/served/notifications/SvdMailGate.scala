/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.notifications


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
