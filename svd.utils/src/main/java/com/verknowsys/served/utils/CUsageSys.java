package com.verknowsys.served.utils;

import com.sun.jna.*;


/**
 *  @author dmilith
 *
 *   BSD code to get system usage info
 */

public interface CUsageSys extends Library {
    public static final CUsageSys instance = (CUsageSys) Native.loadLibrary("usagesys", CUsageSys.class);

    String getProcessUsage(int uid, boolean consoleOutput);

}
