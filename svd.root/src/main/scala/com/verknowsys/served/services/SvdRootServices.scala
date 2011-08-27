package com.verknowsys.served.services


import com.verknowsys.served.db._
import com.verknowsys.served.SvdConfig
import com.verknowsys.served.utils._
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.api._


/**
 *  @author dmilith
 *
 *   Predefined root services
 */
object SvdRootServices {

    lazy val coreginxConfig = SvdServiceConfig(
        name = "Coreginx",
        install = SvdShellOperation("echo INSTALL task: Not implemented") :: Nil,
        start = SvdShellOperation("/Software/Coreginx/sbin/nginx") :: Nil,
        stop = SvdShellOperation("/Software/Coreginx/sbin/nginx -s stop") :: Nil
    )

}

