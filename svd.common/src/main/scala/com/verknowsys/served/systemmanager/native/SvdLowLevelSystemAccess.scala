/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.systemmanager.native


import com.verknowsys.served.utils._


object SvdLowLevelSystemAccess extends Logging {


    def usagesys(uid: Int) = {

        CUsageSys.instance.processDataToLearn(uid)
    }


    log.debug("%s has been initialized".format(this.getClass))

}
