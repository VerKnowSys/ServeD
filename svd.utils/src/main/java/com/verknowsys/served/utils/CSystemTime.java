/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.utils;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface CSystemTime extends Library {
    public static final CSystemTime instance = (CSystemTime) Native.loadLibrary("systemtime", CSystemTime.class);

    boolean adjustSystemTime(double offset);

}

