package com.verknowsys.served.services


import com.verknowsys.served.db._
import com.verknowsys.served.SvdConfig
import com.verknowsys.served.utils._
import com.verknowsys.served.systemmanager.native._
import com.verknowsys.served.api._

import akka.actor.Actor


trait NginxService extends SvdService {
    
    override def configureHook = super.configureHook ::: List(SvdShellOperation(commands = "ls -la /tmp/"))
    
    override def startHook = super.startHook ::: List(SvdShellOperation(commands = "ls -la /var"))
    
    override def afterStartHook = List(SvdShellOperation(commands = "ls -la /Users")) ::: super.afterStartHook
    
}