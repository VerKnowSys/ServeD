package com.verknowsys.served.systemmanager.native


import com.verknowsys.served.systemmanager.acl._
import com.verknowsys.served.utils.git.GitRepository
import java.io.File
import org.apache.commons.io.FileUtils
import akka.util.Logging


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
        val homeDir: String = "/home/",
        val shell: String = "/bin/bash",
        val acls: List[Any] = Nil
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
     * @author dmilith
     */
    def accountWeight =
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
object SvdAccount {
    
    
    /**
     *  @author dmilith
     *  
     *  Returns ACLs of given user
     */
    def aclFor(name: String) = {
        // 2011-02-01 07:03:11 - dmilith - TODO: implement ACL mangling
        
        true
    }
    
<<<<<<< HEAD
    def unapply(x: AnyRef) = x match {
        case a: SvdAccount => Some((a.userName, a.pass, a.uid, a.gid, a.information, a.homeDir, a.shell, a.acls))
        case _ => None
    }


=======
    
    /**
     * @author teamon
     */
    // def apply(a: String, b: String, c: Int, d: Int, e: String, f: String, g: String, h: List[Any]) = new SvdAccount(a, b, c.toInt, d.toInt, e, f, g, h.toList) 
    
    
>>>>>>> More dirty code in SvdACL. Moved traits to case classes. SvdProcess is now case class. More specs for SvdAccount.
    /**
     * @author teamon
     */
    def unapply(line: String) = {
        if(line.startsWith("#")) None
        else {
            line.split(":") match {
<<<<<<< HEAD
                case Array(a, b, c, d, e, f, g) => Some(SvdAccount(a, b, c.toInt, d.toInt, e, f, g, null)) // 2011-02-01 14:52:17 - dmilith - XXX: HACK: ignoring acls
=======
                case Array(a, b, c, d, e, f, g, h) => Some(new SvdAccount(a, b, c.toInt, d.toInt, e, f, g, h.toList))
>>>>>>> More dirty code in SvdACL. Moved traits to case classes. SvdProcess is now case class. More specs for SvdAccount.
                case _ => None
            }
        }
    }
}
