package com.verknowsys.served.utils;

import com.sun.jna.Library;
import com.sun.jna.Native;


/**
 *  @author dmilith
 *
 *   BSD code to get system usage info
 */

public interface CUsageSys extends Library {

    final String arch = System.getProperty("os.arch");
    final String osName = System.getProperty("os.name");

    public static final CUsageSys instance =
        (CUsageSys) Native.loadLibrary(
            "usagesys" + (
                ("i386".equals(arch) && !("Mac OS X".equals(osName))) ? "32" : ""
            ),
            CUsageSys.class
        );

    String getProcessUsage(int uid, boolean consoleOutput);
    String processDataToLearn(int uid);
    boolean isSymlink(String path);

}

