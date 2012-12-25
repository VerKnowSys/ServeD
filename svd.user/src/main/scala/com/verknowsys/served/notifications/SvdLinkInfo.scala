/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.notifications
import java.util.Date


class SvdLinkInfo(val author: String, val channel: String, val message: String) {
    require(author.length > 0)
    require(channel.length > 0)
    require(message.length > 0)

    val date = new Date

}
