/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.api.pools


object SvdPools {
    
    /**
     *  @author dmilith
     *
     *   userPortPool describes a range of user allowed pool of TCP ports
     */
    def userPortPool = 10000 until 60000
    
    
    /**
     *  @author dmilith
     *
     *   svdPortPool describes a range of root allowed pool of TCP ports
     */
    def systemPortPool = 0 until 1024
    
    
    /**
     *  @author dmilith
     *
     *   userUidPool describes a range of system uids for users
     */
    def userUidPool = 10000 until 2147483646 /* NOTE: FreeBSD and Darwin systems support unsigned int: 4294967295 */
    

}
