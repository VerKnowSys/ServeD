/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.utils;

import com.sun.jna.Library;
import com.sun.jna.Native;


/**
 *  @author dmilith, teamon
 *
 *   Interfaces and libraries to low level access to posix system functions
 */ 

public interface CStat extends Library {
    public static final CStat instance = (CStat) Native.loadLibrary("stat", CStat.class);
    
    int getOwner(String path);
    
}
