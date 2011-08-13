package com.verknowsys.served.utils;

import com.sun.jna.*;


/**
 *  @author dmilith, teamon
 *
 *   Interfaces and libraries to low level access to posix system functions
 */ 

public interface CStat extends Library {
    public static final CStat instance = (CStat) Native.loadLibrary("stat", CStat.class);
    
    int getOwner(String path);
    
}
