package com.verknowsys.served.systemmanager.native


import com.verknowsys.served.systemmanager.acl._
import com.verknowsys.served.utils.git.GitRepository
import com.verknowsys.served.utils.Logging

import java.io.File
import org.apache.commons.io.FileUtils
import scala.util.matching.Regex

/**
 * SvdAccount data storage
 * 
 * @author dmilith
 * @author teamon
 * 
 */
case class SvdAccount (
        val userName: String = "guest",
        val pass: String = "x",
        val uid: Int = 1000,
        val gid: Int = 1000,
        val information: String = "No information",
        val homeDir: String = "/tmp",
        val shell: String = "/bin/sh",
        val acls: List[Any] = Nil
        ) extends Logging {

    
    /**
     * Checks if account is for normal user
     * @author teamon
     */
    def isUser = SvdAccount.isUser(this)
    
    
    /**
     * @author teamon 
     */
    override def toString = "SvdAccount(" + userName + ")"


    /**
     * @author dmilith
     */
    def accountWeight: Option[Long] =
        try {
            val elementsSize = FileUtils.sizeOfDirectory(new File(homeDir))
            log.debug("getSvdAccountSize of " + homeDir + " folder: " + elementsSize)
            Some(elementsSize)
        } catch {
            case x: Exception =>
                log.error("accountWeight exception: " + x.getMessage)
                None
        }

}


/**
 * @author teamon
 */
object SvdAccount extends Logging {
    
    
    /**
     * Checks if account is for normal user
     * @author teamon, dmilith
     */
    def isUser(account: SvdAccount) = {
        val unixLikeHome = "/home/.*".r
        val macLikeHome = "/Users/.*".r
        account.homeDir match {
            case unixLikeHome() => true
            case macLikeHome() => true
            case x =>
                log.debug("Value of 'homeDir' for user '%s' is: '%s'. It's system user.".format(account.userName, x, false))
                false
        }
    }
    
    
    /**
     *  @author dmilith
     *  
     *  Returns ACLs of given user
     */
    def aclFor(name: String) = {
        // 2011-02-01 07:03:11 - dmilith - TODO: implement ACL mangling
        
        true
    }
    

    /**
     * @author teamon
     */
    def unapply(x: Any) = x match {
        case a: SvdAccount => Some((a.userName, a.pass, a.uid, a.gid, a.information, a.homeDir, a.shell, a.acls))
        case _ => None
    }


    /**
     * @author teamon
     */
    def unapply(line: String) = {
        if(line.startsWith("#")) None
        else {
            line.split(":") match {
                case Array(a, b, c, d, e, f, g) => Some(new SvdAccount(a, b, c.toInt, d.toInt, e, f, g))
                case null =>
                    log.warn("Something weird happened on unapply in SvdAccount. Got null here!")
                    None
                case _ =>
                    None
            }
        }
    }
}
