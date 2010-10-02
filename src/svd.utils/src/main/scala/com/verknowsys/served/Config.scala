package com.verknowsys.served

import com.verknowsys.served.utils.Properties


object Config {


    /**
    * @author dmilith
    *
    * A very low level settings hardcoded into application
    *
    */

    final val vendorDir = ".svd/"
    
    final val mainPropertiesFile = "svd.properties"
    
    final val loggerPropertiesFile = "logger.properties"

    final val homePath = System.getProperty("user.home") + "/"

    final val systemPasswdFile = etcPath + passwdFileName
    
    final val mainConfigFile = homePath + vendorDir + mainPropertiesFile

    final val mainLoggerFile = homePath + vendorDir + loggerPropertiesFile

    final val props = new Properties(mainConfigFile)
    
    

    /**
    *   @author dmilith
    *   More dynamic settings from main properties file
    */

    val etcPath = props("etcPath") getOrElse {
        props("etcPath") = "/etc/"
        "/etc/"
    }

    val passwdFileName = props("passwdFileName") getOrElse {
        props("passwdFileName") = "passwd"
        "passwd"
    }

    val checkInterval = props.int("checkInterval") getOrElse {
        props("checkInterval") = 1500
        1500
    }

    val sizeMultiplier = props.int("sizeMultiplier") getOrElse {
        props("sizeMultiplier") = 1024
        1024 // bytes to kilobytes
    }



}
