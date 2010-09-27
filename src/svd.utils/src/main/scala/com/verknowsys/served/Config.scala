package com.verknowsys.served


object Config {


  /**
  * @author dmilith
  * 
  * A very low level settings hardcoded into application
  * 
  */
  
  val home = System.getProperty("user.home") + "/"
  
  val vendorDir = ".svd/"

  val propertiesFile = "ServeD.properties"
  
  val systemPasswdFile = "/etc/passwd"

  val checkInterval = 3000 // in ms XXX: should be more for production, but small values will make me see average performance of Maintainer

  val sizeMultiplier = 1024 // bytes to kilobytes
}