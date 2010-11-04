package com.verknowsys.served.maintainer

import com.verknowsys.served.utils.Utils
import com.verknowsys.served.utils.git.GitRepository
import java.io.File
import org.apache.commons.io.FileUtils

/**
 * Account data storage
 * 
 * @author dmilith
 * @author teamon
 * 
 */
case class Account(
        val userName: String = "guest",
        val pass: String = "x",
        val uid: String = "1000",
        val gid: String = "1000",
        val information: String = "No information",
        val homeDir: String = "/home/",
        val shell: String = "/bin/bash"
        ) extends Utils {
            
    // TODO: Handle list size
    def this(a: List[String]) = this(
        userName = a(0),
        pass = a(1),
        uid = a(2),
        gid = a(3),
        information = a(4),
        homeDir = a(5),
        shell = a(6)
    )
    
    /**
     * Checks if account is for normal user
     * @author teamon
     */
    def isUser = homeDir.startsWith("/home")
    
    def equals(that: Account) = this.userName == that.userName && this.uid == that.uid
    
    // def this(a: List[String]) = this(a(0), a(1), a(2), a(3), a(4), a(5), a(6))
    
    // XXX: Remove me!
    def size = {
        try {
            val elementsSize = FileUtils.sizeOfDirectory(new File(homeDir))
            logger.debug("getAccountSize of " + homeDir + " folder: " + elementsSize)
            Some(elementsSize)
        } catch {
            case x: Exception =>
                logger.error("Error: " + x)
                None
        }
    }
}
