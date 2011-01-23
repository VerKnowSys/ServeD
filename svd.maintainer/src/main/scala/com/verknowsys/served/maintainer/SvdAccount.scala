package com.verknowsys.served.maintainer

import com.verknowsys.served.utils.git.GitRepository
import java.io.File

import akka.util.Logging

/**
 * SvdAccount data storage
 * 
 * @author dmilith
 * @author teamon
 * 
 */
class SvdAccount (
        val userName: String = "guest",
        val pass: String = "x",
        val uid: String = "1000",
        val gid: String = "1000",
        val information: String = "No information",
        val homeDir: String = "/home/",
        val shell: String = "/bin/bash"
        ) extends Logging {

    
    /**
     * Checks if account is for normal user
     * @author teamon
     */
    def isUser = homeDir.startsWith("/home")
    
    /**
     * @author teamon 
     */
    override def toString = "SvdAccount(" + userName + ")"
    
    /**
     * @author teamon
     */
    override def equals(that: Any) = that match {
        case a: SvdAccount => this.userName == a.userName && this.uid == a.uid
        case _ => false
    } 
    
    /**
     * @author teamon
     */
    override def hashCode = (31*userName.hashCode)*31 + uid.hashCode
        
    // XXX: Remove me!
    // def size = {
    //     try {
    //         val elementsSize = FileUtils.sizeOfDirectory(new File(homeDir))
    //         log.debug("getSvdAccountSize of " + homeDir + " folder: " + elementsSize)
    //         Some(elementsSize)
    //     } catch {
    //         case x: Exception =>
    //             error("Error: " + x)
    //             None
    //     }
    // }
}

/**
 * @author teamon
 */
object SvdAccount {
    /**
     * @author teamon
     */
    def apply(a: String, b: String, c: String, d: String, e: String, f: String, g: String) = new SvdAccount(a, b, c, d, e, f, g) 

    /**
     * @author teamon
     */
    def unapply(line: String) = {
        if(line.startsWith("#")) None
        else {
            line.split(":") match {
                case Array(a, b, c, d, e, f, g) => Some(SvdAccount(a, b, c, d, e, f, g))
                case _ => None
            }
        }
    }
    
    /**
     * @author teamon
     */
    def unapply(s: AnyRef) = s match {
        case a:SvdAccount => Some((a.userName, a.pass, a.uid, a.gid, a.information, a.homeDir, a.shell))
        case _ => None
    }
}