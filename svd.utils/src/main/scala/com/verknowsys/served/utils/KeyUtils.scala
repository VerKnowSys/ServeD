/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.utils

object KeyUtils extends Logging {
    def load(key: String) = catchException {
        PublicKeyReaderUtil.load(key)
    }
}
