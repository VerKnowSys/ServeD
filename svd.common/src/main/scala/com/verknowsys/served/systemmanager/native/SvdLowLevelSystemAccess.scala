package com.verknowsys.served.systemmanager.native


import com.verknowsys.served._
import com.verknowsys.served.utils._
import com.verknowsys.served.utils.signals._
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.api.acl._
import com.verknowsys.served.utils._
import SvdPOSIX._

import com.sun.jna.{Native, Library}


object SvdLowLevelSystemAccess extends Logging {


    def usagesys(uid: Int) = {
        import CUsageSys._
        CUsageSys.instance.getProcessUsage(uid, false)
    }


    log.debug("%s has been initialized".format(this.getClass))

}