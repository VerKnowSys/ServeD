// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.utils.commiter

import java.util.Date


class Commit(var commitSha1: String, var toRead: Boolean) {
    require(commitSha1.length != 0)

    val date = new Date

    def this(commitSha1: String) = this (commitSha1, true)

}
