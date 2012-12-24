package com.verknowsys.served.systemmanager.native


import com.verknowsys.served.utils._


object SvdLowLevelSystemAccess extends Logging {


    def usagesys(uid: Int) = {

        CUsageSys.instance.processDataToLearn(uid)
    }


    log.debug("%s has been initialized".format(this.getClass))

}
